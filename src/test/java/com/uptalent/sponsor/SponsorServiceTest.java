package com.uptalent.sponsor;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.payload.AuthResponse;
import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.sponsor.service.SponsorService;
import com.uptalent.util.service.AccessVerifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SponsorServiceTest {
    @Mock
    private SponsorRepository sponsorRepository;
    @Mock
    private CredentialsRepository credentialsRepository;
    @Mock
    private AccessVerifyService accessVerifyService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SponsorService sponsorService;

    private Credentials credentials;
    private Sponsor sponsor;
    private Proof proof;

    @Value("${sponsor.initial-kudos-number}")
    private int INITIAL_KUDOS_NUMBER;

    @BeforeEach
    public void setUp() {
        credentials = Credentials.builder()
                .id(1L)
                .email("sponsor.email@gmail.com")
                .password(passwordEncoder.encode("1234567890"))
                .status(AccountStatus.ACTIVE)
                .role(Role.SPONSOR)
                .build();

        sponsor = Sponsor.builder()
                .id(1L)
                .credentials(credentials)
                .fullname("Sponsor")
                .kudos(INITIAL_KUDOS_NUMBER)
                .build();

        proof = Proof.builder()
                .id(1L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(1)
                .status(ProofStatus.PUBLISHED)
                .build();
    }

    @Test
    @DisplayName("[Stage-3.2] [US-1] - Register new Sponsor successfully")
    public void registerNewSponsorSuccessfully() {
        when(sponsorRepository.save(any()))
                .thenReturn(sponsor);

        when(credentialsRepository.save(any()))
                .thenReturn(credentials);

        AuthResponse authResponse = sponsorService.registerSponsor(generateRegistrationRequest());

        assertThat(authResponse).isNotNull();
    }

    @Test
    @DisplayName("[Stage-3.2] [US-1] - Register new Sponsor with earlier occupied email")
    public void registerNewSponsorWithEarlierOccupiedEmail() {
        when(sponsorRepository.save(any()))
                .thenThrow(AccountExistsException.class);

        assertThrows(AccountExistsException.class, () -> sponsorService.registerSponsor(generateRegistrationRequest()));
    }

    @Test
    @DisplayName("[Stage-3.2] [US-3] - Register new Sponsor and forget input some data")
    public void registerNewSponsorAndForgetInputSomeData() {
        SponsorRegistration registrationRequest = generateRegistrationRequest();
        registrationRequest.setFullname(null);

        when(sponsorRepository.save(any()))
                .thenThrow(new MockitoException(""));

        assertThrows(MockitoException.class, () -> sponsorService.registerSponsor(registrationRequest));
    }

    private SponsorRegistration generateRegistrationRequest() {
        SponsorRegistration sponsorRegistration = new SponsorRegistration();

        sponsorRegistration.setFullname(sponsor.getFullname());
        sponsorRegistration.setEmail(sponsor.getCredentials().getEmail());
        sponsorRegistration.setPassword(sponsor.getCredentials().getPassword());

        return sponsorRegistration;
    }

    @Test
    @DisplayName("[Stage-3.2] [US-2] - Get list of kudosed proof successfully")
    public void getListKudosedProofSuccessfully() {
        List<KudosedProof> kudosedProofs = List.of(new KudosedProof(proof.getId(), proof.getIconNumber(), proof.getTitle(), LocalDateTime.now(), 50));

        given(sponsorRepository.findAllKudosedProofBySponsorId(sponsor.getId())).willReturn(kudosedProofs);

        assertEquals(kudosedProofs, sponsorService.getListKudosedProofBySponsorId(sponsor.getId()));
    }

}
