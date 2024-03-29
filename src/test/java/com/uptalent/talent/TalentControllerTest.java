package com.uptalent.talent;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.skill.model.SkillInfo;
import com.uptalent.skill.model.SkillProofInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.controller.TalentController;
import com.uptalent.talent.model.response.TalentStatistic;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.talent.service.TalentService;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;

import com.uptalent.auth.model.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private TalentService talentService;

    @MockBean
    private TalentRepository talentRepository;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private SponsorRepository sponsorRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private CredentialsRepository credentialsRepository;

    @Autowired
    private MockMvc mockMvc;
    private Credentials credentials;
    private Talent talent;
    private Proof proof;
    private Skill javaSkill;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        credentials = Credentials.builder()
                .id(1L)
                .email("dmytro.teliukov@gmail.com")
                .password("1234567890")
                .status(AccountStatus.ACTIVE)
                .role(Role.TALENT)
                .build();
        talent = Talent.builder()
                .id(1L)
                .credentials(credentials)
                .lastname("Teliukov")
                .firstname("Dmytro")
                .build();
        proof = Proof.builder()
                .id(1L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .status(ContentStatus.PUBLISHED)
                .talent(talent)
                .build();

        javaSkill = Skill.builder()
                .id(1L)
                .name("Java")
                .build();

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
                .willReturn(new TalentOwnProfile(talent.getCredentials().getEmail(), LocalDate.now()));

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

        given(jwtTokenProvider.generateJwtToken(anyString(), anyLong(), any(Role.class), anyString()))
                .willReturn(jwtToken);
        HttpServletRequest request = mock(HttpServletRequest.class);

        doAnswer(invocation -> new AuthResponse(jwtToken))
                .when(talentService).addTalent(any(TalentRegistration.class), eq(request));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)));

        response
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    @Order(6)
    @DisplayName("[Stage-1] [US-3] - Register new Talent with earlier occupied email")
    void registerNewTalentWithEarlierOccupiedEmail() throws Exception {
        TalentRegistration registrationRequest = generateRegistrationRequest();

        String exceptionMessage = "The user has already exists with this email";
        doAnswer(invocation -> {
            throw new AccountExistsException(exceptionMessage);
        }).when(talentService).addTalent(any(TalentRegistration.class), any(HttpServletRequest.class));


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

        String exceptionMessage = "The talent has already exists with this email";

        doAnswer(invocation -> {
            throw new AccountExistsException(exceptionMessage);
        }).when(talentService).addTalent(any(TalentRegistration.class), any(HttpServletRequest.class));

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
    @Order(10)
    @DisplayName("[Stage-1] [US-3] - Edit own profile successfully")
    void editOwnProfileSuccessfully() throws Exception {
        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")

                .build();

        TalentOwnProfile expectedDto = new TalentOwnProfile();
        expectedDto.setLastname(editRequest.getLastname());
        expectedDto.setFirstname(editRequest.getFirstname());
        expectedDto.setEmail(talent.getCredentials().getEmail());
        expectedDto.setBirthday(talent.getBirthday());


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
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstname").exists());
    }

/*    @Test
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
*/

    @Test
    @Order(16)
    @DisplayName("[Stage-5] [US-] - Get statistic successfully ")
    void getStatisticSuccessfully() throws Exception {

        SkillProofInfo skillProofInfo = new SkillProofInfo();
        skillProofInfo.setId(javaSkill.getId());
        skillProofInfo.setKudos(100L);
        skillProofInfo.setName(javaSkill.getName());

        ProofDetailInfo proofDetailInfo = ProofDetailInfo.builder()
                .id(proof.getId())
                .content(proof.getContent())
                .published(proof.getPublished())
                .status(proof.getStatus())
                .summary(proof.getSummary())
                .title(proof.getTitle())
                .iconNumber(proof.getIconNumber())
                .kudos(100)
                .skills(Set.of(skillProofInfo))
                .build();

        SkillInfo skillInfo = new SkillInfo();
        skillInfo.setKudos(100L);
        skillInfo.setName(javaSkill.getName());

        TalentStatistic expectedDTO = TalentStatistic.builder()
                .totalCountKudos(100)
                .mostKudosedSkills(Set.of(skillInfo))
                .mostKudosedProof(proofDetailInfo)
                .build();

        given(talentService.getStatistic(anyLong()))
                .willReturn(expectedDTO);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/statistic", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total_count_kudos").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.most_kudosed_skills").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.most_kudosed_proof").exists());
    }

    @Test
    @Order(17)
    @DisplayName("[Stage-5] [US-] - Non-existing profile statistic")
    void getStatisticFailed() throws Exception {

        given(talentService.getStatistic(anyLong()))
                .willThrow(new TalentNotFoundException("Talent was not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/statistic", talent.getId())
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
        registrationRequest.setEmail(talent.getCredentials().getEmail());
        registrationRequest.setPassword(talent.getCredentials().getPassword());


        return registrationRequest;
    }


}