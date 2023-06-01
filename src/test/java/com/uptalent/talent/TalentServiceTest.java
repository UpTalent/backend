package com.uptalent.talent;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.filestore.FileStoreService;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.talent.service.TalentService;
import com.uptalent.util.service.AccessVerifyService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
class TalentServiceTest {

    @Mock
    private TalentRepository talentRepository;
    @Mock
    private CredentialsRepository credentialsRepository;

    @Mock
    private TalentMapper talentMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AccessVerifyService accessVerifyService;
    @Mock
    private FileStoreService fileStoreService;

    @InjectMocks
    private TalentService talentService;

    private Credentials credentials;
    private Talent talent;

    private static final Long nonExistentTalentId = 1000L;

    @BeforeEach
    public void setUp() {
        credentials = Credentials.builder()
                .id(1L)
                .email("dmytro.teliukov@gmail.com")
                .password(passwordEncoder.encode("1234567890"))
                .status(AccountStatus.ACTIVE)
                .role(Role.TALENT)
                .build();
        talent = Talent.builder()
                .id(1L)
                .credentials(credentials)
                .lastname("Teliukov")
                .firstname("Dmytro")

                .build();
    }
    @Test
    @Order(2)
    @DisplayName("[Stage-1] [US-2] - Get talent profile successfully")
    void getTalentProfileSuccessfully() {
        securitySetUp();

        willReturnProfile();

        when(talentMapper.toTalentProfile(any()))
                .thenReturn(new TalentProfile());

        TalentProfile talentProfile = talentService.getTalentProfileById(talent.getId());

        assertThat(talentProfile).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("[Stage-1] [US-2] - Get own profile successfully")
    void getOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();
        doReturn(true).when(accessVerifyService).isPersonalProfile(anyLong(), any(Role.class));

        when(talentMapper.toTalentOwnProfile(any()))
                .thenReturn(new TalentOwnProfile(talent.getCredentials().getEmail(), LocalDate.now()));

        TalentOwnProfile ownProfile = ((TalentOwnProfile) talentService.getTalentProfileById(talent.getId()));

        assertThat(ownProfile).isNotNull();
        assertThat(ownProfile.getEmail()).isEqualTo(talent.getCredentials().getEmail());
    }

    @Test
    @Order(4)
    @DisplayName("[Stage-1] [US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() {

        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.getTalentProfileById(nonExistentTalentId));
    }
/*
    @Test
    @Order(5)
    @DisplayName("[Stage-1] [US-3] - Register new Talent successfully")
    void registerNewTalentSuccessfully() throws MessagingException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        EmailSender emailSender = mock(EmailSender.class);
        String token = UUID.randomUUID().toString();
        // Set up mock interactions and return values
        when(talentRepository.save(any())).thenReturn(talent);
        when(credentialsRepository.save(any())).thenReturn(credentials);

        // Perform the test
        emailSender.sendMail(talent.getCredentials().getEmail(),
                token,
                request.getHeader(HttpHeaders.REFERER),
                talent.getFirstname(),
                credentials.getExpirationDeleting(),
                EmailType.VERIFY);
        talentService.addTalent(generateRegistrationRequest(), request);


    }
*/
    @Test
    @Order(6)
    @DisplayName("[Stage-1] [US-3] - Register new Talent with earlier occupied email")
    void registerNewTalentWithEarlierOccupiedEmail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(talentRepository.save(any()))
                .thenThrow(AccountExistsException.class);

        assertThrows(AccountExistsException.class, () -> talentService.addTalent(generateRegistrationRequest(),request));
    }

    @Test
    @Order(7)
    @DisplayName("[Stage-1] [US-3] - Register new Talent and forget input some data")
    void registerNewTalentAndForgetInputSomeData() {
        TalentRegistration registrationRequest = generateRegistrationRequest();
        HttpServletRequest request = mock(HttpServletRequest.class);

        registrationRequest.setFirstname(null);

        when(talentRepository.save(any()))
                .thenThrow(new MockitoException(""));

        assertThrows(MockitoException.class, () -> talentService.addTalent(registrationRequest, request));
    }

/*
    @Test
    @Order(10)
    @DisplayName("[Stage-1] [US-3] - Edit own profile successfully")
    void editOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")

                .build();

        Talent talentToBeSaved = Talent.builder()
                .firstname(editRequest.getFirstname())
                .lastname(editRequest.getLastname())

                .build();

        when(talentRepository.save(any(Talent.class))).thenReturn(talentToBeSaved);

        talentService.updateTalent(talent.getId(), editRequest);

        verify(talentRepository, times(1)).save(talent);

        assertThat(talent).isNotNull();
        assertThat(talent.getLastname()).isEqualTo("Himonov");
        assertThat(talent.getFirstname()).isEqualTo("Mark");

    }

    @Test
    @Order(11)
    @DisplayName("[Stage-1] [US-3] - Try edit someone else's profile")
    void tryEditSomeoneTalentProfile() {
        securitySetUp();

        willReturnProfile();

        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")

                .build();

        doThrow(new DeniedAccessException("")).when(accessVerifyService)
                .tryGetAccess(anyLong(), any(Role.class), anyString());

        assertThrows(DeniedAccessException.class, () -> talentService.updateTalent(talent.getId(), editRequest));
    }

    @Test
    @Order(12)
    @DisplayName("[Stage-1] [US-3] - Fail editing own profile")
    void failEditingOwnProfile() {
        securitySetUp();

        willReturnOwnProfile();

        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .build();

        assertThrows(NullPointerException.class, () -> talentService.updateTalent(talent.getId(), editRequest));
}
*/
/*     @Test
    @Order(13)
    @DisplayName("[Stage-1] [US-4] - Delete own profile successfully")
    void deleteOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        willDoNothing().given(talentRepository).delete(talent);
        willDoNothing().given(fileStoreService).deleteImageByUserId(talent.getId());

        talentService.deleteTalent(talent.getId());

        verify(talentRepository, times(1)).delete(talent);
    }

    @Test
    @Order(14)
    @DisplayName("[Stage-1] [US-4] - Try delete someone else's profile")
    void tryDeleteSomeoneTalentProfile() {
        securitySetUp();

        willReturnProfile();
        doThrow(new DeniedAccessException("")).when(accessVerifyService)
                .tryGetAccess(anyLong(), any(Role.class), anyString());

        assertThrows(DeniedAccessException.class, () -> talentService.deleteTalent(talent.getId()));

    }

    @Test
    @Order(15)
    @DisplayName("[Stage-1] [US-4] - Delete non-existent profile")
    void deleteNonExistentProfile() {
        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.deleteTalent(nonExistentTalentId));
    }
*/

    private void securitySetUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThat(securityContext.getAuthentication()).isEqualTo(authentication);
    }

    private void willReturnOwnProfile() {
        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));

    }

    private void willReturnProfile() {
        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));

    }

    private TalentRegistration generateRegistrationRequest() {
        TalentRegistration registrationRequest = new TalentRegistration();
        registrationRequest.setLastname(talent.getLastname());
        registrationRequest.setFirstname(talent.getFirstname());
        registrationRequest.setEmail(talent.getCredentials().getEmail());
        registrationRequest.setPassword(talent.getCredentials().getPassword());
        return registrationRequest;
    }
}