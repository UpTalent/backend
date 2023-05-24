package com.uptalent.config;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.filestore.FileStoreService;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
@Transactional
public class SchedulerConfig {
    private final SponsorRepository sponsorRepository;
    private final CredentialsRepository credentialsRepository;
    private final FileStoreService fileStoreService;
    private final TalentRepository talentRepository;
    private final ProofRepository proofRepository;

    @Async
    @Scheduled(cron = "0 */5 * * * ?")
    //@Scheduled(cron = "0 10 11 * * ?")
    public void deletePermanently(){
        List<Credentials> credentials = credentialsRepository.findAllByStatusPermanentDelete(LocalDateTime.now());

        List<Sponsor> sponsors = credentials.stream().map(Credentials::getSponsor).filter(Objects::nonNull).toList();
        List<Talent> talents = credentials.stream().map(Credentials::getTalent).filter(Objects::nonNull).toList();

        sponsors.forEach(s -> fileStoreService.deleteImageByUserIdAndRole(s.getId(), s.getCredentials().getRole()));
        talents.forEach(t -> fileStoreService.deleteImageByUserIdAndRole(t.getId(), t.getCredentials().getRole()));

        sponsorRepository.updateSponsorDeleteData(sponsors.stream().map(Sponsor::getId).collect(Collectors.toList()));
        talentRepository.updateTalentDeleteData(talents.stream().map(Talent::getId).collect(Collectors.toList()));
        proofRepository.updateProofsDeleteData(talents.stream().flatMap(t -> t.getProofs().stream().map(Proof::getId)).collect(Collectors.toList()));
        credentialsRepository.updateCredentialsDeleteData(credentials.stream().map(Credentials::getId).collect(Collectors.toList()));
    }

    private void deleteTalentSkills(List<Talent> talents) {
        for (Talent talent : talents) {
            Set<Skill> skills = talent.getSkills();

            for (Skill skill : skills) {
                skill.getTalents().remove(talent);
            }

            talent.getSkills().clear();
            talentRepository.save(talent);
        }
    }

    @Async
    @Scheduled(cron = "0 */5 * * * ?")
    public void deleteNotVerify(){
        List<Credentials> credentials = credentialsRepository.findAllNotVerifiedExpired(LocalDateTime.now());

        List<Sponsor> sponsors = credentials.stream().map(Credentials::getSponsor).filter(Objects::nonNull).toList();
        List<Talent> talents = credentials.stream().map(Credentials::getTalent).filter(Objects::nonNull).toList();


        deleteTalentSkills(talents);

        credentialsRepository.deleteAll(credentials);
        sponsorRepository.deleteAll(sponsors);
        talentRepository.deleteAll(talents);
    }
}

