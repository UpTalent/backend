package com.uptalent.talent;

import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.talent.exception.TalentExistsException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentLogin;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import com.uptalent.payload.AuthResponse;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.talent.service.TalentService;
import com.uptalent.util.service.AccessVerifyService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TalentServiceTest {

    @Mock
    private TalentRepository talentRepository;

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

    @InjectMocks
    private TalentService talentService;

    private Talent talent;

    private static final Long nonExistentTalentId = 1000L;

    @BeforeEach
    public void setUp() {
        talent = Talent.builder()
                .id(1L)
                .lastname("Teliukov")
                .firstname("Dmytro")
                .email("dmytro.teliukov@gmail.com")
                .password(passwordEncoder.encode("1234567890"))
                .skills(Set.of("Java", "Spring"))
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("[Stage-1] [US-1] - Get all talents successfully")
    void getAllTalentsSuccessfully() {
        List<Talent> talents = Arrays.asList(
                talent,
                Talent.builder()
                        .id(2L)
                        .lastname("Himonov")
                        .firstname("Mark")
                        .email("mark.himonov@gmail.com")
                        .password("123")
                        .skills(Set.of("Java", "Spring"))
                        .build()
        );

        Page<Talent> talentsPage = new PageImpl<>(talents);

        List<TalentGeneralInfo> talentGeneralInfos = Arrays.asList(
                TalentGeneralInfo.builder()
                        .id(talent.getId())
                        .lastname(talent.getLastname())
                        .firstname(talent.getFirstname())
                        .skills(talent.getSkills())
                        .build(),
                TalentGeneralInfo.builder()
                        .id(2L)
                        .lastname("Himonov")
                        .firstname("Mark")
                        .skills(Set.of("Java", "Spring"))
                        .build()
        );

        when(talentMapper.toTalentGeneralInfos(anyList())).thenReturn(talentGeneralInfos);

        when(talentRepository.findAllByOrderByIdDesc(any(PageRequest.class))).thenReturn(talentsPage);

        PageWithMetadata<TalentGeneralInfo> result = talentService.getAllTalents(0, 9);

        verify(talentRepository, times(1)).findAllByOrderByIdDesc(PageRequest.of(0, 9));

        verify(talentMapper, times(1)).toTalentGeneralInfos(talents);

        assertThat(result.getContent()).isEqualTo(talentGeneralInfos);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(talentGeneralInfos.get(0).getId());
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
        doReturn(true).when(accessVerifyService).isPersonalProfile(anyLong());

        when(talentMapper.toTalentOwnProfile(any()))
                .thenReturn(new TalentOwnProfile(talent.getEmail(), LocalDate.now()));

        TalentOwnProfile ownProfile = ((TalentOwnProfile) talentService.getTalentProfileById(talent.getId()));

        assertThat(ownProfile).isNotNull();
        assertThat(ownProfile.getEmail()).isEqualTo(talent.getEmail());
    }

    @Test
    @Order(4)
    @DisplayName("[Stage-1] [US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() {

        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.getTalentProfileById(nonExistentTalentId));
    }

    @Test
    @Order(5)
    @DisplayName("[Stage-1] [US-3] - Register new Talent successfully")
    void registerNewTalentSuccessfully() {

        when(talentRepository.save(any()))
                .thenReturn(talent);

        AuthResponse authResponse = talentService.addTalent(generateRegistrationRequest());

        assertThat(authResponse).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("[Stage-1] [US-3] - Register new Talent with earlier occupied email")
    void registerNewTalentWithEarlierOccupiedEmail() {

        String exceptionMessage = "The talent has already exists with email [" + talent.getEmail() + "]";

        when(talentRepository.save(any()))
                .thenThrow(new TalentExistsException(exceptionMessage));

        assertThrows(TalentExistsException.class, () -> talentService.addTalent(generateRegistrationRequest()));
    }

    @Test
    @Order(7)
    @DisplayName("[Stage-1] [US-3] - Register new Talent and forget input some data")
    void registerNewTalentAndForgetInputSomeData() {
        TalentRegistration registrationRequest = generateRegistrationRequest();
        registrationRequest.setFirstname(null);

        when(talentRepository.save(any()))
                .thenThrow(new MockitoException(""));

        assertThrows(MockitoException.class, () -> talentService.addTalent(registrationRequest));
    }

    @Test
    @Order(8)
    @DisplayName("[Stage-1] [US-3] - Log in successfully")
    void loginSuccessfully() {
        securitySetUp();

        TalentLogin loginRequest = new TalentLogin(talent.getEmail(), "12345");

        when(talentRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(talent));

        when(passwordEncoder.matches(loginRequest.getPassword(), talent.getPassword())).thenReturn(true);

        AuthResponse loggedInUser = talentService.login(loginRequest);

        verify(talentRepository, times(1)).findByEmail(loginRequest.getEmail());

        assertThat(loggedInUser).isNotNull();
    }

    @Test
    @Order(9)
    @DisplayName("[Stage-1] [US-3] - Fail attempt of log in")
    void failLoginWithBadCredentials() {
        securitySetUp();

        TalentLogin loginRequestWithBadPassword =
                new TalentLogin(talent.getEmail(), "another_password");

        when(talentRepository.findByEmail(loginRequestWithBadPassword.getEmail())).thenReturn(Optional.of(talent));

        when(passwordEncoder.matches(loginRequestWithBadPassword.getPassword(), talent.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> talentService.login(loginRequestWithBadPassword));

        TalentLogin loginRequestWithBadEmail =
                new TalentLogin("mark.gimonov@gmail.com", "12345");

        when(talentRepository.findByEmail(loginRequestWithBadEmail.getEmail())).thenReturn(Optional.empty());

        assertThrows(TalentNotFoundException.class, () -> talentService.login(loginRequestWithBadEmail));
    }

    @Test
    @Order(10)
    @DisplayName("[Stage-1] [US-3] - Edit own profile successfully")
    void editOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .skills(Set.of("Java", "Spring"))
                .build();

        Talent talentToBeSaved = Talent.builder()
                .firstname(editRequest.getFirstname())
                .lastname(editRequest.getLastname())
                .skills(editRequest.getSkills())
                .build();

        when(talentRepository.save(any(Talent.class))).thenReturn(talentToBeSaved);

        talentService.updateTalent(talent.getId(), editRequest);

        verify(talentRepository, times(1)).save(talent);

        assertThat(talent).isNotNull();
        assertThat(talent.getLastname()).isEqualTo("Himonov");
        assertThat(talent.getFirstname()).isEqualTo("Mark");
        assertThat(talent.getSkills()).contains("Java", "Spring");
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
                .skills(Set.of("Java", "Spring"))
                .build();

        doThrow(new DeniedAccessException("")).when(accessVerifyService).tryGetAccess(anyLong(), anyString());

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

    @Test
    @Order(13)
    @DisplayName("[Stage-1] [US-4] - Delete own profile successfully")
    void deleteOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        willDoNothing().given(talentRepository).delete(talent);

        talentService.deleteTalent(talent.getId());

        verify(talentRepository, times(1)).delete(talent);
    }

    @Test
    @Order(14)
    @DisplayName("[Stage-1] [US-4] - Try delete someone else's profile")
    void tryDeleteSomeoneTalentProfile() {
        securitySetUp();

        willReturnProfile();
        doThrow(new DeniedAccessException("")).when(accessVerifyService).tryGetAccess(anyLong(), anyString());

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
        registrationRequest.setEmail(talent.getEmail());
        registrationRequest.setPassword(talent.getPassword());
        registrationRequest.setSkills(talent.getSkills());

        return registrationRequest;
    }
}