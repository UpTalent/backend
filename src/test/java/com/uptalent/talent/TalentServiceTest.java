package com.uptalent.talent;

import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.exception.DeniedAccessException;
import com.uptalent.talent.model.exception.TalentExistsException;
import com.uptalent.talent.model.exception.TalentNotFoundException;
import com.uptalent.talent.model.request.TalentEditRequest;
import com.uptalent.talent.model.request.TalentLoginRequest;
import com.uptalent.talent.model.request.TalentRegistrationRequest;
import com.uptalent.talent.model.response.TalentDTO;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import com.uptalent.talent.model.response.TalentResponse;
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
    @DisplayName("[US-1] - Get all talents successfully")
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

        List<TalentDTO> talentDTOs = Arrays.asList(
                TalentDTO.builder()
                        .id(talent.getId())
                        .lastname(talent.getLastname())
                        .firstname(talent.getFirstname())
                        .skills(talent.getSkills())
                        .build(),
                TalentDTO.builder()
                        .id(2L)
                        .lastname("Himonov")
                        .firstname("Mark")
                        .skills(Set.of("Java", "Spring"))
                        .build()
        );

        when(talentMapper.toTalentDTOs(anyList())).thenReturn(talentDTOs);

        when(talentRepository.findAllByOrderByIdDesc(any(PageRequest.class))).thenReturn(talentsPage);

        PageWithMetadata<TalentDTO> result = talentService.getAllTalents(0, 9);

        verify(talentRepository, times(1)).findAllByOrderByIdDesc(PageRequest.of(0, 9));

        verify(talentMapper, times(1)).toTalentDTOs(talents);

        assertThat(result.getContent()).isEqualTo(talentDTOs);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(talentDTOs.get(0).getId());
    }

    @Test
    @Order(2)
    @DisplayName("[US-2] - Get talent profile successfully")
    void getTalentProfileSuccessfully() {
        securitySetUp();

        willReturnProfile();

        when(talentMapper.toTalentProfileDTO(any()))
                .thenReturn(new TalentProfileDTO());

        TalentProfileDTO talentProfile = talentService.getTalentProfileById(talent.getId());

        assertThat(talentProfile).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("[US-2] - Get own profile successfully")
    void getOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        when(talentMapper.toTalentOwnProfileDTO(any()))
                .thenReturn(new TalentOwnProfileDTO(talent.getEmail(), LocalDate.now()));

        TalentOwnProfileDTO ownProfile = ((TalentOwnProfileDTO) talentService.getTalentProfileById(talent.getId()));

        assertThat(ownProfile).isNotNull();
        assertThat(ownProfile.getEmail()).isEqualTo(talent.getEmail());
    }

    @Test
    @Order(4)
    @DisplayName("[US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() {

        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.getTalentProfileById(nonExistentTalentId));
    }

    @Test
    @Order(5)
    @DisplayName("[US-3] - Register new Talent successfully")
    void registerNewTalentSuccessfully() {

        when(talentRepository.save(any()))
                .thenReturn(talent);

        TalentResponse talentResponse = talentService.addTalent(generateRegistrationRequest());

        assertThat(talentResponse).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("[US-3] - Register new Talent with earlier occupied email")
    void registerNewTalentWithEarlierOccupiedEmail() {

        String exceptionMessage = "The talent has already exists with email [" + talent.getEmail() + "]";

        when(talentRepository.save(any()))
                .thenThrow(new TalentExistsException(exceptionMessage));

        assertThrows(TalentExistsException.class, () -> talentService.addTalent(generateRegistrationRequest()));
    }

    @Test
    @Order(7)
    @DisplayName("[US-3] - Register new Talent and forget input some data")
    void registerNewTalentAndForgetInputSomeData() {
        TalentRegistrationRequest registrationRequest = generateRegistrationRequest();
        registrationRequest.setFirstname(null);

        when(talentRepository.save(any()))
                .thenThrow(new MockitoException(""));

        assertThrows(MockitoException.class, () -> talentService.addTalent(registrationRequest));
    }

    @Test
    @Order(8)
    @DisplayName("[US-3] - Log in successfully")
    void loginSuccessfully() {
        securitySetUp();

        TalentLoginRequest loginRequest = new TalentLoginRequest(talent.getEmail(), "12345");

        when(talentRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(talent));

        when(passwordEncoder.matches(loginRequest.password(), talent.getPassword())).thenReturn(true);

        TalentResponse loggedInUser = talentService.login(loginRequest);

        verify(talentRepository, times(1)).findByEmail(loginRequest.email());

        assertThat(loggedInUser).isNotNull();
    }

    @Test
    @Order(9)
    @DisplayName("[US-3] - Fail attempt of log in")
    void failLoginWithBadCredentials() {
        securitySetUp();

        TalentLoginRequest loginRequestWithBadPassword =
                new TalentLoginRequest(talent.getEmail(), "another_password");

        when(talentRepository.findByEmail(loginRequestWithBadPassword.email())).thenReturn(Optional.of(talent));

        when(passwordEncoder.matches(loginRequestWithBadPassword.password(), talent.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> talentService.login(loginRequestWithBadPassword));

        TalentLoginRequest loginRequestWithBadEmail =
                new TalentLoginRequest("mark.gimonov@gmail.com", "12345");

        when(talentRepository.findByEmail(loginRequestWithBadEmail.email())).thenReturn(Optional.empty());

        assertThrows(TalentNotFoundException.class, () -> talentService.login(loginRequestWithBadEmail));
    }

    @Test
    @Order(10)
    @DisplayName("[US-3] - Edit own profile successfully")
    void editOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        TalentEditRequest editRequest = TalentEditRequest.builder()
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
    @DisplayName("[US-3] - Try edit someone else's profile")
    void tryEditSomeoneTalentProfile() {
        securitySetUp();

        willReturnProfile();

        TalentEditRequest editRequest = TalentEditRequest.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .skills(Set.of("Java", "Spring"))
                .build();

        assertThrows(DeniedAccessException.class, () -> talentService.updateTalent(talent.getId(), editRequest));
    }

    @Test
    @Order(12)
    @DisplayName("[US-3] - Fail editing own profile")
    void failEditingOwnProfile() {
        securitySetUp();

        willReturnOwnProfile();

        TalentEditRequest editRequest = TalentEditRequest.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .build();

        assertThrows(NullPointerException.class, () -> talentService.updateTalent(talent.getId(), editRequest));
}

    @Test
    @Order(13)
    @DisplayName("[US-4] - Delete own profile successfully")
    void deleteOwnProfileSuccessfully() {
        securitySetUp();

        willReturnOwnProfile();

        willDoNothing().given(talentRepository).delete(talent);

        talentService.deleteTalent(talent.getId());

        verify(talentRepository, times(1)).delete(talent);
    }

    @Test
    @Order(14)
    @DisplayName("[US-4] - Try delete someone else's profile")
    void tryDeleteSomeoneTalentProfile() {
        securitySetUp();

        willReturnProfile();

        assertThrows(DeniedAccessException.class, () -> talentService.deleteTalent(talent.getId()));

    }

    @Test
    @Order(15)
    @DisplayName("[US-4] - Delete non-existent profile")
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

        given(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .willReturn(talent.getEmail());
    }

    private void willReturnProfile() {
        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));

        given(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .willReturn("john.doe@gmail.com");
    }

    private TalentRegistrationRequest generateRegistrationRequest() {
        TalentRegistrationRequest registrationRequest = new TalentRegistrationRequest();

        registrationRequest.setLastname(talent.getLastname());
        registrationRequest.setFirstname(talent.getFirstname());
        registrationRequest.setEmail(talent.getEmail());
        registrationRequest.setPassword(talent.getPassword());
        registrationRequest.setSkills(talent.getSkills());

        return registrationRequest;
    }
}