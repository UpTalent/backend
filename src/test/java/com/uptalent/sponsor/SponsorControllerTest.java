package com.uptalent.sponsor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.sponsor.controller.SponsorController;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.request.SponsorEdit;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.model.response.SponsorProfile;
import com.uptalent.sponsor.service.SponsorService;
import com.uptalent.talent.exception.DeniedAccessException;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SponsorController.class)
@Disabled
public class SponsorControllerTest {
    @MockBean
    private SponsorService sponsorService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @MockBean
    private CredentialsRepository credentialsRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

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
                .password("1234567890")
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
    public void registerNewSponsorSuccessfully() throws Exception {
        SponsorRegistration registrationRequest = generateRegistrationRequest();

        String jwtToken = "token";

        given(jwtTokenProvider.generateJwtToken(anyString(), anyLong(), any(Role.class), anyString()))
                .willReturn(jwtToken);

        when(sponsorService.registerSponsor(any(SponsorRegistration.class)))
                .thenReturn(new AuthResponse(jwtToken));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/sponsors")
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
    @DisplayName("[Stage-3.2] [US-1] - Register new Sponsor with earlier occupied email")
    void registerNewSponsorWithEarlierOccupiedEmail() throws Exception {
        SponsorRegistration registrationRequest = generateRegistrationRequest();

        String exceptionMessage = "The user has already exists with this email";

        when(sponsorService.registerSponsor(any(SponsorRegistration.class)))
                .thenThrow(new AccountExistsException(exceptionMessage));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/sponsors")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(exceptionMessage));
    }

    @Test
    @DisplayName("[Stage-3.2] [US-1] - Register new Sponsor and forget input some data")
    void registerNewSponsorAndForgetInputSomeData() throws Exception {
        SponsorRegistration registrationRequest = generateRegistrationRequest();
        registrationRequest.setFullname(null);
        registrationRequest.setEmail(null);

        String exceptionMessage = "The talent has already exists with this email";

        when(sponsorService.registerSponsor(any(SponsorRegistration.class)))
                .thenThrow(new AccountExistsException(exceptionMessage));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/sponsors")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)));

        response
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullname").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists());
    }

    @Test
    @DisplayName("[Stage-3.2] [US-1] - Edit own profile successfully")
    void editOwnProfileSuccessfully() throws Exception {

        SponsorEdit editRequest = SponsorEdit.builder()
                .fullname("test case")
                .build();
        SponsorProfile expectedDto = new SponsorProfile();
        expectedDto.setFullname(editRequest.getFullname());

        given(sponsorService.editSponsor(anyLong(), any(SponsorEdit.class)))
                .willReturn(expectedDto);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/sponsors/{id}", sponsor.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)));
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullname").exists())
                .andReturn().getResponse().getContentAsString();
    }
    @Test
    @DisplayName("[Stage-3.2] [US-1] - Try edit someone else's profile")
    void tryEditSomeoneTalentProfile() throws Exception {
        SponsorEdit editRequest = SponsorEdit.builder()
                .fullname("test case")
                .build();

        given(sponsorService.editSponsor(anyLong(), any(SponsorEdit.class)))
                .willThrow(new DeniedAccessException("You are not allowed to edit this sponsor"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/sponsors/{sponsorId}", sponsor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest))
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }
    @Test
    @DisplayName("[Stage-3.2] [US-1] - Fail editing own profile")
    void failEditingOwnProfile() throws Exception {
        SponsorEdit editRequest = SponsorEdit.builder()
                .fullname("")
                .build();


        given(sponsorService.editSponsor(anyLong(), any(SponsorEdit.class)))
                .willThrow(NullPointerException.class);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/sponsors/{sponsorId}", sponsor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest))
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullname").exists());
    }
    private SponsorRegistration generateRegistrationRequest() {
        SponsorRegistration sponsorRegistration = new SponsorRegistration();

        sponsorRegistration.setFullname(sponsor.getFullname());
        sponsorRegistration.setEmail(sponsor.getCredentials().getEmail());
        sponsorRegistration.setPassword(sponsor.getCredentials().getPassword());

        return sponsorRegistration;
    }
}
