package com.uptalent.talent;

import com.uptalent.filestore.FileStoreOperation;
import com.uptalent.filestore.FileStoreService;
import com.uptalent.filestore.exception.EmptyFileException;
import com.uptalent.filestore.exception.FailedToUploadFileException;
import com.uptalent.filestore.exception.IncorrectFileFormatException;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.model.exception.DeniedAccessException;
import com.uptalent.talent.model.exception.TalentExistsException;
import com.uptalent.talent.model.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.request.TalentEditRequest;
import com.uptalent.talent.model.request.TalentLoginRequest;
import com.uptalent.talent.model.request.TalentRegistrationRequest;
import com.uptalent.talent.model.response.TalentDTO;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import com.uptalent.talent.model.response.TalentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.apache.http.entity.ContentType.IMAGE_PNG;

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
    @Value("${aws.bucket.name}")
    private String BUCKET_NAME;

    public PageWithMetadata<TalentDTO> getAllTalents(int page, int size){
        Page<Talent> talentPage = talentRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
        List<TalentDTO> talentDTOs = talentMapper.toTalentDTOs(talentPage.getContent());
        return new PageWithMetadata<>(talentDTOs, talentPage.getTotalPages());
    }

    @Transactional
    public TalentResponse addTalent(TalentRegistrationRequest talent){
        if (talentRepository.existsByEmailIgnoreCase(talent.getEmail())){
            throw new TalentExistsException("The talent has already exists with email [" + talent.getEmail() + "]");
        }

        var savedTalent = talentRepository.save(Talent.builder()
                    .password(passwordEncoder.encode(talent.getPassword()))
                    .email(talent.getEmail())
                    .firstname(talent.getFirstname())
                    .lastname(talent.getLastname())
                    .skills(new LinkedHashSet<>(talent.getSkills()))
                    .build());

        String jwtToken = jwtTokenProvider.generateJwtToken(savedTalent.getEmail());
        return new TalentResponse(savedTalent.getId(), jwtToken);
    }

    @Transactional
    public TalentResponse login(TalentLoginRequest loginRequest) {
        String email = loginRequest.email();
        Talent foundTalent = talentRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found by email [" + email + "]"));

        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        var authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtTokenProvider.generateJwtToken(email);
        return new TalentResponse(foundTalent.getId(), jwtToken);
    }

    public TalentProfileDTO getTalentProfileById(Long id) {
        Talent foundTalent = getTalentById(id);

        if (isPersonalProfile(foundTalent)) {
            return talentMapper.toTalentOwnProfileDTO(foundTalent);
        } else {
            return talentMapper.toTalentProfileDTO(foundTalent);
        }
    }

    @Transactional
    public TalentOwnProfileDTO updateTalent(Long id, TalentEditRequest updatedTalent) {
        Talent talentToUpdate = getTalentById(id);
        if(!isPersonalProfile(talentToUpdate)) {
            throw new DeniedAccessException("You are not allowed to edit this talent");
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

        return talentMapper.toTalentOwnProfileDTO(savedTalent);
    }
    @Transactional
    public void deleteTalent(Long id) {
        Talent talentToDelete = getTalentById(id);
        if (!isPersonalProfile(talentToDelete)) {
            throw new DeniedAccessException("You are not allowed to delete this talent");
        } else {
            talentRepository.delete(talentToDelete);
        }
    }

    @Transactional
    public void uploadImage(Long id, MultipartFile image, FileStoreOperation operation) {
        Talent talent = getTalentById(id);
        if(!isPersonalProfile(talent)) {
            throw new DeniedAccessException("You are not allowed to edit this talent");
        }

        isFileEmpty(image);
        isImage(image);

        Map<String, String> metadata = extractMetadata(image);
        String imageType = operation.equals(FileStoreOperation.UPLOAD_AVATAR) ? "avatar" : "banner";

        String path = String.format("%s/%s", BUCKET_NAME, id);
        String filename = String.format("%s.%s", imageType, getFileExtension(image));

        String imageUrl = generateImageUrl(id, filename);

        try {
            fileStoreService.save(path, filename, Optional.of(metadata), image.getInputStream());
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

    private boolean isPersonalProfile(Talent talent) {
        String authEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return authEmail.equalsIgnoreCase(talent.getEmail());
    }

    private static Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private static void isImage(MultipartFile file) {
        if(!Arrays.asList(IMAGE_JPEG.getMimeType(), IMAGE_PNG.getMimeType()).contains(file.getContentType())){
            throw new IncorrectFileFormatException("File must be an image");
        }
    }

    private static void isFileEmpty(MultipartFile file) {
        if(file.isEmpty()){
            throw new EmptyFileException("File must not be empty");
        }
    }

    private String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        } else {
            return "";
        }
    }

    private String generateImageUrl(Long id, String filename) {
        // TODO: Refactor the imageUrl generation logic
        return String.format("https://%s.s3.amazonaws.com/%s/%s",
                BUCKET_NAME, id, filename);
    }
}
