package com.uptalent.sponsor.service;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.email.EmailSender;
import com.uptalent.email.model.EmailType;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.KudosHistoryMapper;
import com.uptalent.mapper.SponsorMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.proof.kudos.model.response.KudosedProofHistory;
import com.uptalent.skill.repository.SkillKudosHistoryRepository;
import com.uptalent.sponsor.exception.IllegalAddingKudosException;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.request.IncreaseKudos;
import com.uptalent.sponsor.model.request.SponsorEdit;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.model.response.SponsorProfile;
import com.uptalent.sponsor.model.response.SponsorRating;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.util.service.AccessVerifyService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.uptalent.credentials.model.enums.Role.SPONSOR;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SponsorService {
    private final SponsorRepository sponsorRepository;
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessVerifyService accessVerifyService;
    private final KudosHistoryMapper kudosHistoryMapper;
    private final SkillKudosHistoryRepository skillKudosHistoryRepository;

    private final EmailSender sender;
    @Value("${sponsor.initial-kudos-number}")
    private long INITIAL_KUDOS_NUMBER;
    @Value("${kudos.max-value}")
    private long KUDOS_MAX_VALUE;

    public void registerSponsor(SponsorRegistration sponsorRegistration, HttpServletRequest request) throws MessagingException {
        if (credentialsRepository.existsByEmailIgnoreCase(sponsorRegistration.getEmail())){
            throw new AccountExistsException("The user has already exists with email [" + sponsorRegistration.getEmail() + "]");
        }
        String token = UUID.randomUUID().toString();
        var credentials = Credentials.builder()
                .email(sponsorRegistration.getEmail())
                .password(passwordEncoder.encode(sponsorRegistration.getPassword()))
                .status(AccountStatus.TEMPORARY_DELETED)
                .expirationDeleting(LocalDateTime.now().plusMinutes(10))
                //.expirationDeleting(LocalDateTime.now().plusSeconds(30))
                .deleteToken(token)
                .role(SPONSOR)
                .verified(false)
                .build();

        credentialsRepository.save(credentials);

        var savedSponsor = sponsorRepository.save(Sponsor.builder()
                .credentials(credentials)
                .fullname(sponsorRegistration.getFullname())
                .kudos(INITIAL_KUDOS_NUMBER)
                .build());
        String link = "https://white-plant-071773303.3.azurestaticapps.net/";
        sender.sendMail(
                credentials.getEmail(),
                token,
                //request.getHeader(HttpHeaders.REFERER),
                link,
                savedSponsor.getFullname(),
                credentials.getExpirationDeleting(),
                EmailType.VERIFY
        );
    }

    @Transactional(readOnly = true)
    public PageWithMetadata<KudosedProof> getListKudosedProofBySponsorId(Long sponsorId, int page, int size) {
        String errorMessage = "You do not have permission to the list";
        accessVerifyService.tryGetAccess(sponsorId, SPONSOR, errorMessage);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<KudosedProof> kudosedProofPage = sponsorRepository.findAllKudosedProofBySponsorId(sponsorId, pageRequest);

        kudosedProofPage.stream().forEach(kp -> kp.setSkills(skillKudosHistoryRepository
                                .findSumSkillsBySponsorIdAndProofId(sponsorId, kp.getProofId())));

        return new PageWithMetadata<>(kudosedProofPage.getContent(), kudosedProofPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageWithMetadata<KudosedProofHistory> getListKudosedProofHistoryBySponsorIdAndProofId(Long sponsorId, Long proofId,
                                                                                                 int page, int size) {
        String errorMessage = "You do not have permission to the list";
        accessVerifyService.tryGetAccess(sponsorId, SPONSOR, errorMessage);

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<KudosHistory> kudosedProofHistoriesPage =
                sponsorRepository.findAllKudosedProofHistoryBySponsorIdAndProofId(sponsorId, proofId, pageRequest);

        List<KudosedProofHistory> kudosedProofHistories = kudosedProofHistoriesPage.getContent().stream()
                .map(kudosHistoryMapper::toKudosedProofHistory).toList();

        return new PageWithMetadata<>(kudosedProofHistories,
                kudosedProofHistoriesPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public SponsorProfile getSponsorProfileById(Long sponsorId) {
        Sponsor foundSponsor = getSponsorById(sponsorId);
        accessVerifyService.tryGetAccess(
                sponsorId,
                SPONSOR,
                "You are not allowed to get this sponsor"
        );

        return SponsorMapper.toSponsorProfile(foundSponsor);
    }

    public SponsorProfile editSponsor(Long sponsorId, SponsorEdit updatedSponsor) {
        Sponsor sponsorToUpdate = getSponsorById(sponsorId);
        accessVerifyService.tryGetAccess(
                sponsorId,
                SPONSOR,
                "You are not allowed to edit this sponsor"
        );

        sponsorToUpdate.setFullname(updatedSponsor.getFullname());
        Sponsor savedSponsor = sponsorRepository.save(sponsorToUpdate);
        return SponsorMapper.toSponsorProfile(savedSponsor);
    }

    public void addKudos(Long sponsorId, IncreaseKudos increaseKudos) {
        Sponsor sponsorToUpdate = getSponsorById(sponsorId);
        accessVerifyService.tryGetAccess(
                sponsorId,
                SPONSOR,
                "You are not allowed to edit this sponsor"
        );

        if (KUDOS_MAX_VALUE - sponsorToUpdate.getKudos() < increaseKudos.getBalance()) {
            throw new IllegalAddingKudosException("You reached max value of balance");
        }

        sponsorToUpdate.setKudos(sponsorToUpdate.getKudos() + increaseKudos.getBalance());
        sponsorRepository.save(sponsorToUpdate);
    }

    public List<SponsorRating> getSponsorRating() {
        Long talentId = accessVerifyService.getPrincipalId();

        PageRequest limitRequest = PageRequest.of(0, 10);
        return sponsorRepository.getSponsorRatingByTalentId(talentId, limitRequest)
                .getContent();
    }
    private Sponsor getSponsorById(Long sponsorId) {
        return sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new SponsorNotFoundException("Sponsor was not found"));
    }

    public void deleteSponsor(Long sponsorId, HttpServletRequest request) throws MessagingException {
        accessVerifyService.tryGetAccess(
                sponsorId,
                SPONSOR,
                "You are not allowed to delete this sponsor"
        );

        Sponsor deletedSponsor = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new SponsorNotFoundException("Sponsor was not found"));
        if(isStatusEquals(deletedSponsor, AccountStatus.ACTIVE)) {
            String token = UUID.randomUUID().toString();
            //deletedSponsor.getCredentials().setExpirationDeleting(LocalDateTime.now().plusDays(7));
            deletedSponsor.getCredentials().setExpirationDeleting(LocalDateTime.now().plusMinutes(10));
            deletedSponsor.getCredentials().setStatus(AccountStatus.TEMPORARY_DELETED);
            deletedSponsor.getCredentials().setDeleteToken(token);
            String link = "https://white-plant-071773303.3.azurestaticapps.net/";
            sender.sendMail(
                    deletedSponsor.getCredentials().getEmail(),
                    token,
                    //request.getHeader(HttpHeaders.REFERER),
                    link,
                    deletedSponsor.getFullname(),
                    deletedSponsor.getCredentials().getExpirationDeleting(),
                    EmailType.RESTORE
            );
            sponsorRepository.save(deletedSponsor);
        }
        else {
            throw new SponsorNotFoundException("Sponsor was not found");
        }
    }

    private boolean isStatusEquals(Sponsor sponsor, AccountStatus status){
        return sponsor.getCredentials().getStatus().equals(status);
    }
}
