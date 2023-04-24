package com.uptalent.talent.service;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.filestore.FileStoreService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {
    private final TalentRepository talentRepository;
    private final TalentMapper talentMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccessVerifyService accessVerifyService;
    private final CredentialsRepository credentialsRepository;
    private final FileStoreService fileStoreService;

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
        credentialsRepository.delete(talentToDelete.getCredentials());
        fileStoreService.deleteImageByUserId(id);
        talentRepository.delete(talentToDelete);
    }

    private Talent getTalentById(Long id) {
        return talentRepository.findById(id)
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found"));
    }
}
