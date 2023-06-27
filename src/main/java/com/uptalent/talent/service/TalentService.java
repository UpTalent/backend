package com.uptalent.talent.service;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.email.EmailSender;
import com.uptalent.email.model.EmailType;
import com.uptalent.filestore.FileStoreService;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.ProofMapper;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofTalentDetailInfo;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.skill.model.SkillInfo;
import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.repository.SkillRepository;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.exception.TalentIllegalEditingException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.property.TalentAgeRange;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.talent.model.response.TalentStatistic;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.util.service.AccessVerifyService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.uptalent.credentials.model.enums.Role.TALENT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentService {
    private final TalentRepository talentRepository;
    private final TalentMapper talentMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccessVerifyService accessVerifyService;
    private final CredentialsRepository credentialsRepository;
    private final SkillRepository skillRepository;
    private final SponsorRepository sponsorRepository;
    private final ProofRepository proofRepository;
    private final TalentAgeRange talentAgeRange;
    private final ProofMapper proofMapper;

    private final EmailSender sender;


    public PageWithMetadata<TalentGeneralInfo> getAllTalents(int page, int size, String [] skills){
        Page<Talent> talentPage = retrieveAllTalents(page, size, skills);
        List<TalentGeneralInfo> talentGeneralInfos = talentMapper.toTalentGeneralInfos(talentPage.getContent());
        return new PageWithMetadata<>(talentGeneralInfos, talentPage.getTotalPages());
    }

    @Transactional
    public void addTalent(TalentRegistration talentRegistration, HttpServletRequest request) throws MessagingException {
        if (credentialsRepository.existsByEmailIgnoreCase(talentRegistration.getEmail())){
            throw new AccountExistsException("The user has already exists with email [" + talentRegistration.getEmail() + "]");
        }
        String token = UUID.randomUUID().toString();
        var credentials = Credentials.builder()
                .email(talentRegistration.getEmail())
                .password(passwordEncoder.encode(talentRegistration.getPassword()))
                .status(AccountStatus.TEMPORARY_DELETED)
                .role(TALENT)
                .expirationDeleting(LocalDateTime.now().plusMinutes(10))
                //.expirationDeleting(LocalDateTime.now().plusSeconds(30))
                .deleteToken(token)
                .verified(false)
                .build();

        credentialsRepository.save(credentials);

        var savedTalent = talentRepository.save(Talent.builder()
                    .credentials(credentials)
                    .firstname(talentRegistration.getFirstname())
                    .lastname(talentRegistration.getLastname())
                    .build());

        updateSkillsIfExists(talentRegistration.getSkills(), savedTalent);
        String link = "https://white-plant-071773303.3.azurestaticapps.net/";
        sender.sendMail(
                credentials.getEmail(),
                token,
                //request.getHeader(HttpHeaders.REFERER),
                link,
                savedTalent.getFirstname(),
                credentials.getExpirationDeleting(),
                EmailType.VERIFY
        );
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


        talentToUpdate.setLastname(updatedTalent.getLastname());
        talentToUpdate.setFirstname(updatedTalent.getFirstname());
        clearSkillsFromTalent(talentToUpdate);
        updateSkillsIfExists(updatedTalent.getSkills(),talentToUpdate);

        LocalDate birthday = updatedTalent.getBirthday();

        if(birthday != null) {
            if (birthday.isBefore(LocalDate.now().minusYears(talentAgeRange.getMaxAge())) ||
                    birthday.isAfter(LocalDate.now().minusYears(talentAgeRange.getMinAge()))) {
                throw new TalentIllegalEditingException(talentAgeRange.getErrorMessage());
            }
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
    public void deleteTalent(Long id, HttpServletRequest request) throws MessagingException {
        Talent talentToDelete = getTalentById(id);
        accessVerifyService.tryGetAccess(
                id,
                talentToDelete.getCredentials().getRole(),
                "You are not allowed to delete this talent"
        );
        talentToDelete.getCredentials().setExpirationDeleting(LocalDateTime.now().plusMinutes(10));
        //talentToDelete.getCredentials().setExpirationDeleting(LocalDateTime.now().plusSeconds(10));
        talentToDelete.getCredentials().setStatus(AccountStatus.TEMPORARY_DELETED);
        String token = UUID.randomUUID().toString();
        talentToDelete.getCredentials().setDeleteToken(token);
        String link = "https://white-plant-071773303.3.azurestaticapps.net/";
        sender.sendMail(
                talentToDelete.getCredentials().getEmail(),
                token,
                //request.getHeader(HttpHeaders.REFERER),
                link,
                talentToDelete.getFirstname(),
                talentToDelete.getCredentials().getExpirationDeleting(),
                EmailType.RESTORE
        );
        talentRepository.save(talentToDelete);
    }


    public TalentStatistic getStatistic(Long talentId) {
        Talent talent = getTalentById(talentId);
        Long totalCountKudos = talentRepository.getTotalCountKudosByTalentId(talentId);
        totalCountKudos = (totalCountKudos == null) ? 0L : totalCountKudos;
        Set<SkillInfo> mostKudosedSkills = getMostKudosedSkills(talentId);
        ProofDetailInfo mostKudosedProof = getMostKudosedProof(talentId, accessVerifyService.getRole());

        return TalentStatistic.builder()
                .totalCountKudos(totalCountKudos)
                .mostKudosedSkills(mostKudosedSkills)
                .mostKudosedProof(mostKudosedProof)
                .build();
    }

    private Talent getTalentById(Long id) {
        return talentRepository.findByIdAndCredentialsVerified(id)
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found"));
    }
    private Set<Skill> getAllMappedSkills(Set <SkillTalentInfo> skillTalentInfo, Talent talent) {
        return skillRepository.findAllById(
                       skillTalentInfo.stream()
                                .map(SkillTalentInfo::getId)
                                .collect(Collectors.toSet())
                )
                .stream()
                .peek(skill -> skill.getTalents().add(talent))
                .collect(Collectors.toSet());
    }
    private void updateSkillsIfExists(Set <SkillTalentInfo> skillTalentInfo, Talent talent) {
        Optional.ofNullable(skillTalentInfo)
                .filter(skills -> !skills.isEmpty())
                .map(skills -> getAllMappedSkills(skillTalentInfo, talent))
                .ifPresent(talent::setSkills);
    }
    private void clearSkillsFromTalent(Talent talent) {
        talent.getSkills().forEach(skill -> skill.getTalents().remove(talent));
        talent.getSkills().clear();
        talentRepository.save(talent);
    }

    private Page<Talent> retrieveAllTalents(int page, int size, String[] skills) {
        if (skills != null)
            return talentRepository.filterAllBySkills(skills, skills.length,
                    PageRequest.of(page, size, Sort.by("id").descending()));
        else
            return talentRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
    }

    private Set<SkillInfo> getMostKudosedSkills(Long talentId) {
        PageRequest limitSkills = PageRequest.of(0, 3);
        return new LinkedHashSet<>(skillRepository
                .getMostKudosedSkillsByTalentId(talentId, limitSkills)
                .getContent());
    }

    private ProofDetailInfo getMostKudosedProof(Long talentId, Role role) {
        PageRequest limitProof = PageRequest.of(0, 1);
        Page<Proof> mostKudosedProofResult = proofRepository.getMostKudosedProofByTalentId(talentId, limitProof);
        if (mostKudosedProofResult.getTotalElements() == 0) {
            return null;
        } else {
            Proof proof = mostKudosedProofResult.getContent().get(0);
            if (role.equals(TALENT)) {
                return proofMapper.toProofTalentDetailInfo(proof, verifyOwnProof(talentId));
            } else {
                return proofMapper.toProofSponsorDetailInfo(proof, sponsorRepository
                        .sumKudosBySponsorAndProof(accessVerifyService.getPrincipalId(), proof.getId()));
            }
        }


    }

    private boolean verifyOwnProof(Long talentId) {
        return accessVerifyService.getPrincipalId().equals(talentId);
    }
}
