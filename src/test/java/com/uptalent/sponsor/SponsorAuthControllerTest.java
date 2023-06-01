package com.uptalent.sponsor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.auth.controller.AuthController;
import com.uptalent.auth.model.request.AuthLogin;
import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.auth.service.AuthService;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.sponsor.model.entity.Sponsor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
public class SponsorAuthControllerTest {
    @MockBean
    private AuthService authService;
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
    }
    @Test
    @Order(1)
    @DisplayName("[US-3] - Log in successfully")
    void loginSuccessfully() throws Exception {
        AuthLogin loginRequest = new AuthLogin(sponsor.getCredentials().getEmail(), sponsor.getCredentials().getPassword());

        String jwtToken = "token";

        given(jwtTokenProvider.generateJwtToken(anyString(), anyLong(), any(Role.class), anyString()))
                .willReturn(jwtToken);

        given(authService.login(any(AuthLogin.class)))
                .willReturn(new AuthResponse(jwtToken));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
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
    @Order(2)
    @DisplayName("[US-3] - Fail attempt of log in")
    void failLoginWithBadCredentials() throws Exception {
        AuthLogin loginRequestWithBadCredentials =
                new AuthLogin(sponsor.getCredentials().getEmail(), "another_password");

        given(authService.login(loginRequestWithBadCredentials))
                .willThrow(new BadCredentialsException("Bad credentials"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestWithBadCredentials)));
        response
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andReturn().getResponse().getContentAsString();
    }
}
