package com.uptalent.sponsor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.payload.AuthResponse;
import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.proof.kudos.model.response.KudosedProofDetail;
import com.uptalent.proof.kudos.model.response.KudosedProofHistory;
import com.uptalent.proof.kudos.model.response.KudosedProofInfo;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.sponsor.controller.SponsorController;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.service.SponsorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SponsorController.class)
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
    @DisplayName("[Stage-3.2] [US-2] - Get list of kudosed proof successfully")
    public void getListKudosedProofSuccessfully() throws Exception {
        List<KudosedProofDetail> kudosedProofDetails =
                List.of(new KudosedProofDetail(
                        new KudosedProofInfo(proof.getId(), proof.getIconNumber(), proof.getTitle(), 50),
                        List.of(new KudosedProofHistory(LocalDateTime.now(), 50))));

        when(sponsorService.getListKudosedProofDetailsBySponsorId(anyLong()))
                .thenReturn(kudosedProofDetails);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/sponsors/{sponsorId}/kudos",
                                sponsor.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].proof_info").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].proof_info.id").value(proof.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].histories[0].kudos").value(50));


    }

    private SponsorRegistration generateRegistrationRequest() {
        SponsorRegistration sponsorRegistration = new SponsorRegistration();

        sponsorRegistration.setFullname(sponsor.getFullname());
        sponsorRegistration.setEmail(sponsor.getCredentials().getEmail());
        sponsorRegistration.setPassword(sponsor.getCredentials().getPassword());

        return sponsorRegistration;
    }
}
