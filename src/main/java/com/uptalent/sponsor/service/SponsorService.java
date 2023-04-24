package com.uptalent.sponsor.service;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.KudosHistoryMapper;
import com.uptalent.payload.AuthResponse;
import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.response.KudosedProofHistory;
import com.uptalent.proof.kudos.model.response.KudosedProofDetail;
import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.proof.kudos.model.response.KudosedProofInfo;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.util.service.AccessVerifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.uptalent.credentials.model.enums.Role.SPONSOR;
import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SponsorService {
    private final SponsorRepository sponsorRepository;
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccessVerifyService accessVerifyService;
    private final KudosHistoryMapper kudosHistoryMapper;

    @Value("${sponsor.initial-kudos-number}")
    private int INITIAL_KUDOS_NUMBER;

    public AuthResponse registerSponsor(SponsorRegistration sponsorRegistration) {
        if (credentialsRepository.existsByEmailIgnoreCase(sponsorRegistration.getEmail())){
            throw new AccountExistsException("The user has already exists with email [" + sponsorRegistration.getEmail() + "]");
        }

        var credentials = Credentials.builder()
                .email(sponsorRegistration.getEmail())
                .password(passwordEncoder.encode(sponsorRegistration.getPassword()))
                .status(AccountStatus.ACTIVE)
                .role(SPONSOR)
                .build();

        credentialsRepository.save(credentials);

        var savedSponsor = sponsorRepository.save(Sponsor.builder()
                .credentials(credentials)
                .fullname(sponsorRegistration.getFullname())
                .kudos(INITIAL_KUDOS_NUMBER)
                .build());

        String jwtToken = jwtTokenProvider.generateJwtToken(
                savedSponsor.getCredentials().getEmail(),
                savedSponsor.getId(),
                savedSponsor.getCredentials().getRole(),
                savedSponsor.getFullname()
        );
        return new AuthResponse(jwtToken);
    }

    @Transactional(readOnly = true)
    public List<KudosedProofDetail> getListKudosedProofDetailsBySponsorId(Long sponsorId) {
        String errorMessage = "You do not have permission to the list";
        accessVerifyService.tryGetAccess(sponsorId, SPONSOR, errorMessage);

        List<KudosedProofDetail> kudosedProofDetails =
                collectKudosedProofDetailsBySponsorId(sponsorId);
        Comparator<KudosedProofDetail> sortByHistory = Comparator.comparing(k -> k.getHistories().get(0).getSent());

        kudosedProofDetails.sort(Collections.reverseOrder(sortByHistory));

        return kudosedProofDetails;
    }

    private List<KudosedProofDetail> collectKudosedProofDetailsBySponsorId(Long sponsorId) {
        return sponsorRepository.findAllKudosedProofBySponsorId(sponsorId)
                .stream()
                .collect(groupingBy(kudosHistoryMapper::toKudosedProofInfo))
                .entrySet()
                .stream()
                .map(entry -> {
                    KudosedProofInfo kudosedProofInfo = entry.getKey();
                    List<KudosedProofHistory> histories = kudosHistoryMapper.toKudosedProofHistories(entry.getValue());
                    int totalSumKudos = histories.stream().mapToInt(KudosedProofHistory::getKudos).sum();
                    kudosedProofInfo.setTotalSumKudos(totalSumKudos);
                    return new KudosedProofDetail(kudosedProofInfo, histories);
                })
                .collect(Collectors.toList());
    }
}
