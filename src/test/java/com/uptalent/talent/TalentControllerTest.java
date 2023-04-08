package com.uptalent.talent;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.jwt.JwtTokenProvider;

import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.controller.TalentController;
import com.uptalent.talent.service.TalentService;
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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TalentController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TalentControllerTest {
    @Value("${jwt.secret}")
    private String secret;

    @MockBean
    TalentService talentService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    MockMvc mockMvc;
    private Talent talent;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        talent = Talent.builder()
                .id(1L)
                .lastname("Teliukov")
                .firstname("Dmytro")
                .email("dmytro.teliukov@gmail.com")
                .password("1234567890")
                .skills(Set.of("Java", "Spring"))
                .build();

    }

    @Test
    @Order(1)
    @DisplayName("[Stage-1] [US-1] - Get all talents successfully")
    void getAllTalentsSuccessfully() throws Exception {
        List<TalentGeneralInfo> talentGeneralInfos = Arrays.asList(
                TalentGeneralInfo.builder()
                        .id(talent.getId())
                        .lastname(talent.getLastname())
                        .firstname(talent.getFirstname())
                        .skills(talent.getSkills()).build(),
                TalentGeneralInfo.builder()
                        .id(2L)
                        .lastname("Himonov")
                        .firstname("Mark")
                        .skills(Set.of("Java", "Spring")).build()
        );

        given(talentService.getAllTalents(0, 9))
                .willReturn(new PageWithMetadata<>(talentGeneralInfos, 1));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents")
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists());
    }

    @Test
    @Order(2)
    @DisplayName("[Stage-1] [US-2] - Get talent profile successfully")
    void getTalentProfileSuccessfully() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willReturn(new TalentProfile());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").doesNotExist());
    }

    @Test
    @Order(3)
    @DisplayName("[Stage-1] [US-2] - Get own profile successfully")
    void getOwnProfileSuccessfully() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willReturn(new TalentOwnProfile(talent.getEmail(), LocalDate.now()));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists());
    }

    @Test
    @Order(4)
    @DisplayName("[Stage-1] [US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willThrow(new TalentNotFoundException("Talent was not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @Order(5)
    @DisplayName("[Stage-1] [US-3] - Register new Talent successfully")
    void registerNewTalentSuccessfully() throws Exception {
        TalentRegistration registrationRequest = generateRegistrationRequest();

        String jwtToken = "token";

        given(jwtTokenProvider.generateJwtToken(any(Talent.class)))
                .willReturn(jwtToken);

        when(talentService.addTalent(any(TalentRegistration.class)))
                .thenReturn(new AuthResponse(jwtToken));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)));

        response
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").value(jwtToken));
    }

    @Test
    @Order(6)
    @DisplayName("[Stage-1] [US-3] - Register new Talent with earlier occupied email")
    void registerNewTalentWithEarlierOccupiedEmail() throws Exception {
        TalentRegistration registrationRequest = generateRegistrationRequest();

        String exceptionMessage = "The talent has already exists with email [" + talent.getEmail() + "]";

        when(talentService.addTalent(any(TalentRegistration.class)))
                .thenThrow(new TalentExistsException(exceptionMessage));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(exceptionMessage));
    }

    @Test
    @Order(7)
    @DisplayName("[Stage-1] [US-3] - Register new Talent and forget input some data")
    void registerNewTalentAndForgetInputSomeData() throws Exception {
        TalentRegistration registrationRequest = generateRegistrationRequest();
        registrationRequest.setLastname(null);
        registrationRequest.setFirstname(null);

        String exceptionMessage = "The talent has already exists with email [" + talent.getEmail() + "]";

        when(talentService.addTalent(any(TalentRegistration.class)))
                .thenThrow(new TalentExistsException(exceptionMessage));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)));

        response
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstname").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastname").exists());
    }

    @Test
    @Order(8)
    @DisplayName("[Stage-1] [US-3] - Log in successfully")
    void loginSuccessfully() throws Exception {
        TalentLogin loginRequest = new TalentLogin(talent.getEmail(), talent.getPassword());

        String jwtToken = "token";

        given(jwtTokenProvider.generateJwtToken(any(Talent.class)))
                .willReturn(jwtToken);

        given(talentService.login(any(TalentLogin.class)))
                .willReturn(new AuthResponse(jwtToken));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)));
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").value(jwtToken))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(9)
    @DisplayName("[Stage-1] [US-3] - Fail attempt of log in")
    void failLoginWithBadCredentials() throws Exception {
        TalentLogin loginRequestWithBadCredentials =
                new TalentLogin(talent.getEmail(), "another_password");

        given(talentService.login(loginRequestWithBadCredentials))
                .willThrow(new BadCredentialsException("Bad credentials"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestWithBadCredentials)));
        response
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(10)
    @DisplayName("[Stage-1] [US-3] - Edit own profile successfully")
    void editOwnProfileSuccessfully() throws Exception {
        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .skills(Set.of("Java", "Spring"))
                .build();

        TalentOwnProfile expectedDto = new TalentOwnProfile();
        expectedDto.setLastname(editRequest.getLastname());
        expectedDto.setFirstname(editRequest.getFirstname());
        expectedDto.setEmail(talent.getEmail());
        expectedDto.setBirthday(talent.getBirthday());
        expectedDto.setSkills(editRequest.getSkills());

        given(talentService.updateTalent(anyLong(), any(TalentEdit.class)))
                .willReturn(expectedDto);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)));
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(11)
    @DisplayName("[Stage-1] [US-3] - Try edit someone else's profile")
    void tryEditSomeoneTalentProfile() throws Exception {
        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .skills(Set.of("Java", "Spring"))
                .build();

        given(talentService.updateTalent(anyLong(), any(TalentEdit.class)))
                .willThrow(new DeniedAccessException("You are not allowed to edit this talent"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{id}", talent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest))
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @Order(12)
    @DisplayName("[Stage-1] [US-3] - Fail editing own profile")
    void failEditingOwnProfile() throws Exception {
        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .build();

        given(talentService.updateTalent(anyLong(), any(TalentEdit.class)))
                .willThrow(NullPointerException.class);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{id}", talent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest))
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.skills").exists());
    }

    @Test
    @Order(13)
    @DisplayName("[Stage-1] [US-4] - Delete own profile successfully")
    void deleteOwnProfileSuccessfully() throws Exception {
        willDoNothing().given(talentService).deleteTalent(talent.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(14)
    @DisplayName("[Stage-1] [US-4] - Try delete someone else's profile")
    void tryDeleteSomeoneTalentProfile() throws Exception {
        willThrow(new DeniedAccessException("You are not allowed to delete this talent"))
                .given(talentService)
                .deleteTalent(talent.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @Order(15)
    @DisplayName("[Stage-1] [US-4] - Delete non-existent profile")
    void deleteNonExistentProfile() throws Exception {
        willThrow(new TalentNotFoundException("Talent was not found"))
                .given(talentService)
                .deleteTalent(talent.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
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