package com.uptalent.talent.controller;


import com.uptalent.jwt.JwtTokenProvider;

import com.uptalent.talent.TalentService;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.exception.DeniedAccessException;
import com.uptalent.talent.model.exception.TalentNotFoundException;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.util.Date;
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

    @Autowired
    MockMvc mockMvc;
    private Talent talent;

    @BeforeEach
    public void setUp() {
        talent = Talent.builder()
                .id(1L)
                .lastname("Teliukov")
                .firstname("Dmytro")
                .email("dmytro.teliukov@gmail.com")
                .password("12345")
                .skills(Set.of("Java", "Spring"))
                .build();

    }

    @Test
    @Order(2)
    @DisplayName("[US-2] - Get talent profile successfully")
    void getTalentProfileSuccessfully() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willReturn(new TalentProfileDTO());

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
    @DisplayName("[US-2] - Get own profile successfully")
    void getOwnProfileSuccessfully() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willReturn(new TalentOwnProfileDTO(talent.getEmail(), new Date()));

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
    @DisplayName("[US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willThrow(new TalentNotFoundException("Talent was not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Talent was not found"));
    }

    @Test
    @Order(13)
    @DisplayName("[US-4] - Delete own profile successfully")
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
    @DisplayName("[US-4] - Try delete someone else's profile")
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("You are not allowed to delete this talent"));
    }

    @Test
    @Order(15)
    @DisplayName("[US-4] - Delete non-existent profile")
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Talent was not found"));
    }


}