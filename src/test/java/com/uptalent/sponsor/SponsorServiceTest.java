package com.uptalent.sponsor;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.request.SponsorEdit;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.sponsor.service.SponsorService;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.util.service.AccessVerifyService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

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

    @Mock
    private AuthenticationManager authenticationManager;
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
/*
    @Test
    @DisplayName("[Stage-3.2] [US-1] - Register new Sponsor successfully")
    public void registerNewSponsorSuccessfully() throws MessagingException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(sponsorRepository.save(any()))
                .thenReturn(sponsor);

        when(credentialsRepository.save(any()))
                .thenReturn(credentials);

        sponsorService.registerSponsor(generateRegistrationRequest(), request);

        // Add your assertions to verify the expected behavior or outcome
        // For example, you can verify that the sponsor is saved or perform additional checks
        verify(sponsorRepository, times(1)).save(any());
        verify(credentialsRepository, times(1)).save(any());
    }
*/
    @Test
    @DisplayName("[Stage-3.2] [US-1] - Register new Sponsor with earlier occupied email")
    public void registerNewSponsorWithEarlierOccupiedEmail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(sponsorRepository.save(any()))
                .thenThrow(AccountExistsException.class);

        assertThrows(AccountExistsException.class, () -> sponsorService.registerSponsor(generateRegistrationRequest(), request));
    }

    @Test
    @DisplayName("[Stage-3.2] [US-3] - Register new Sponsor and forget input some data")
    public void registerNewSponsorAndForgetInputSomeData() {
        SponsorRegistration registrationRequest = generateRegistrationRequest();
        HttpServletRequest request = mock(HttpServletRequest.class);
        registrationRequest.setFullname(null);

        when(sponsorRepository.save(any()))
                .thenThrow(new MockitoException(""));

        assertThrows(MockitoException.class, () -> sponsorService.registerSponsor(registrationRequest, request));
    }


    @Test
    @DisplayName("[Stage-3.2] [US-1] - Edit own profile successfully")
    void editOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        SponsorEdit editRequest = SponsorEdit.builder()
                .fullname("test case")
                .build();

        Sponsor sponsorToSave = Sponsor.builder()
                .id(sponsor.getId())
                .credentials(sponsor.getCredentials())
                .fullname(editRequest.getFullname())
                .kudos(sponsor.getKudos())
                .build();

        when(sponsorRepository.save(any(Sponsor.class))).thenReturn(sponsorToSave);

        sponsorService.editSponsor(sponsor.getId(), editRequest);

        verify(sponsorRepository, times(1)).save(sponsor);

        assertThat(sponsor).isNotNull();
    }
    @Test
    @DisplayName("[Stage-3.2] [US-1] - Try edit someone else's profile")
    void tryEditSomeoneTalentProfile() {
        securitySetUp();

        willReturnProfile();

        SponsorEdit editRequest = SponsorEdit.builder()
                .fullname("test case")
                .build();


        doThrow(new DeniedAccessException("")).when(accessVerifyService)
                .tryGetAccess(anyLong(), any(Role.class), anyString());

        assertThrows(DeniedAccessException.class, () -> sponsorService.editSponsor(sponsor.getId(), editRequest));
    }
    @Test
    @DisplayName("[Stage-3.2] [US-1] - Fail editing own profile")
    void failEditingOwnProfile() {
        securitySetUp();

        willReturnOwnProfile();

        SponsorEdit editRequest = SponsorEdit.builder()
                .fullname("")
                .build();

        assertThrows(NullPointerException.class, () -> sponsorService.editSponsor(sponsor.getId(), editRequest));
    }
    private SponsorRegistration generateRegistrationRequest() {
        SponsorRegistration sponsorRegistration = new SponsorRegistration();

        sponsorRegistration.setFullname(sponsor.getFullname());
        sponsorRegistration.setEmail(sponsor.getCredentials().getEmail());
        sponsorRegistration.setPassword(sponsor.getCredentials().getPassword());

        return sponsorRegistration;
    }

    private void securitySetUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThat(securityContext.getAuthentication()).isEqualTo(authentication);
    }
    private void willReturnOwnProfile() {
        given(sponsorRepository.findById(sponsor.getId()))
                .willReturn(Optional.of(sponsor));

    }
    private void willReturnProfile() {
        given(sponsorRepository.findById(sponsor.getId()))
                .willReturn(Optional.of(sponsor));

    }
}
