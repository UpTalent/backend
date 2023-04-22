package com.uptalent.talent.service;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.filestore.FileStoreOperation;
import com.uptalent.filestore.FileStoreService;
import com.uptalent.filestore.exception.FailedToUploadFileException;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.exception.EmptySkillsException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentLogin;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import com.uptalent.payload.AuthResponse;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.util.service.AccessVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.uptalent.util.ImageUtils.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {
    private final TalentRepository talentRepository;
    private final TalentMapper talentMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileStoreService fileStoreService;
    private final AccessVerifyService accessVerifyService;
    private final CredentialsRepository credentialsRepository;

    @Value("${aws.bucket.name}")
    private String BUCKET_NAME;

    public PageWithMetadata<TalentGeneralInfo> getAllTalents(int page, int size){
        Page<Talent> talentPage = talentRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
        List<TalentGeneralInfo> talentGeneralInfos = talentMapper.toTalentGeneralInfos(talentPage.getContent());
        return new PageWithMetadata<>(talentGeneralInfos, talentPage.getTotalPages());
    }

    @Transactional
    public AuthResponse addTalent(TalentRegistration talent){
        if (credentialsRepository.existsByEmailIgnoreCase(talent.getEmail())){
            throw new AccountExistsException("The user has already exists with email [" + talent.getEmail() + "]");
        }

        if(talent.getSkills().isEmpty()){
            throw new EmptySkillsException("Skills should not be empty");
        }

        var credentials = Credentials.builder()
                .email(talent.getEmail())
                .password(passwordEncoder.encode(talent.getPassword()))
                .status(AccountStatus.ACTIVE)
                .role(Role.TALENT)
                .build();

        credentialsRepository.save(credentials);

        var savedTalent = talentRepository.save(Talent.builder()
                    .credentials(credentials)
                    .firstname(talent.getFirstname())
                    .lastname(talent.getLastname())
                    .skills(new LinkedHashSet<>(talent.getSkills()))
                    .build());

        String jwtToken = jwtTokenProvider.generateJwtToken(
                savedTalent.getCredentials().getEmail(),
                savedTalent.getId(),
                savedTalent.getCredentials().getRole(),
                savedTalent.getFirstname()
        );
        return new AuthResponse(jwtToken);
    }

    @Transactional
    public AuthResponse login(TalentLogin loginRequest) {
        String email = loginRequest.getEmail();
        Talent foundTalent = credentialsRepository.findTalentByEmailIgnoreCase(email)
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found by email [" + email + "]"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), foundTalent.getCredentials().getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        var authenticationToken = new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword());
        var authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtTokenProvider.generateJwtToken(
                foundTalent.getCredentials().getEmail(),
                foundTalent.getId(),
                foundTalent.getCredentials().getRole(),
                foundTalent.getFirstname()
        );
        return new AuthResponse(jwtToken);
    }

    public TalentProfile getTalentProfileById(Long id) {
        Talent foundTalent = getTalentById(id);

        if (accessVerifyService.isPersonalProfile(id, foundTalent.getCredentials().getRole())) {
            return talentMapper.toTalentOwnProfile(foundTalent);
        } else {
            return talentMapper.toTalentProfile(foundTalent);
        }
    }

    @Transactional
    public TalentOwnProfile updateTalent(Long id, TalentEdit updatedTalent) {
        Talent talentToUpdate = getTalentById(id);
        accessVerifyService.tryGetAccess(
                id,
                talentToUpdate.getCredentials().getRole(),
                "You are not allowed to edit this talent"
        );

        if(updatedTalent.getSkills().isEmpty()){
            throw new EmptySkillsException("Skills should not be empty");
        }

        talentToUpdate.setLastname(updatedTalent.getLastname());
        talentToUpdate.setFirstname(updatedTalent.getFirstname());
        talentToUpdate.setSkills(new LinkedHashSet<>(updatedTalent.getSkills()));

        if(updatedTalent.getBirthday() != null) {
            talentToUpdate.setBirthday(updatedTalent.getBirthday());
        }
        if(updatedTalent.getLocation() != null) {
            talentToUpdate.setLocation(updatedTalent.getLocation());
        }
        if(updatedTalent.getAboutMe() != null) {
            talentToUpdate.setAboutMe(updatedTalent.getAboutMe());
        }

        Talent savedTalent = talentRepository.save(talentToUpdate);

        return talentMapper.toTalentOwnProfile(savedTalent);
    }

    @Transactional
    public void deleteTalent(Long id) {
        Talent talentToDelete = getTalentById(id);
        accessVerifyService.tryGetAccess(
                id,
                talentToDelete.getCredentials().getRole(),
                "You are not allowed to delete this talent"
        );
        talentRepository.delete(talentToDelete);
    }

    @Transactional
    public void uploadImage(Long id, MultipartFile image, FileStoreOperation operation) {
        Talent talent = getTalentById(id);
        accessVerifyService.tryGetAccess(
                id,
                talent.getCredentials().getRole(),
                "You are not allowed to edit this talent"
        );

        isFileEmpty(image);
        isImage(image);

        Map<String, String> metadata = extractMetadata(image);
        String imageType = operation.equals(FileStoreOperation.UPLOAD_AVATAR) ? "avatar" : "banner";
        String path = String.format("%s/%s", BUCKET_NAME, id);
        String filename = String.format("%s.%s", imageType, getFileExtension(image));
        String imageUrl = generateImageUrl(id, filename);

        try(InputStream is = resizeImage(image)) {
            fileStoreService.save(path, filename, Optional.of(metadata), is);
            if(operation.equals(FileStoreOperation.UPLOAD_AVATAR)) {
                talent.setAvatar(imageUrl);
            } else {
                talent.setBanner(imageUrl);
            }
        } catch (IOException e) {
            throw new FailedToUploadFileException(e.getMessage());
        }
        talentRepository.save(talent);
    }

    private Talent getTalentById(Long id) {
        return talentRepository.findById(id)
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found"));
    }

    private static Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private String generateImageUrl(Long id, String filename) {
        // TODO: Refactor the imageUrl generation logic
        return String.format("https://%s.s3.amazonaws.com/%s/%s",
                BUCKET_NAME, id, filename);
    }
}
