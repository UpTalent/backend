package com.uptalent.talent.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.jwt.JwtTokenProvider;

import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.talent.TalentService;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.exception.DeniedAccessException;
import com.uptalent.talent.model.exception.TalentNotFoundException;
import com.uptalent.talent.model.request.TalentEditRequest;
import com.uptalent.talent.model.response.TalentDTO;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
                .password("12345")
                .skills(Set.of("Java", "Spring"))
                .build();

    }

    @Test
    @Order(1)
    @DisplayName("[US-1] - Get all talents successfully")
    void getAllTalentsSuccessfully() throws Exception {
        List<TalentDTO> talentDTOs = Arrays.asList(
                TalentDTO.builder()
                        .id(talent.getId())
                        .lastname(talent.getLastname())
                        .firstname(talent.getFirstname())
                        .skills(talent.getSkills()).build(),
                TalentDTO.builder()
                        .id(2L)
                        .lastname("Himonov")
                        .firstname("Mark")
                        .skills(Set.of("Java", "Spring")).build()
        );

        given(talentService.getAllTalents(0, 9))
                .willReturn(new PageWithMetadata<>(talentDTOs, 1));

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
                .willReturn(new TalentOwnProfileDTO(talent.getEmail(), LocalDate.now()));

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
    @Order(10)
    @DisplayName("[US-3] - Edit own profile successfully")
    void editOwnProfileSuccessfully() throws Exception {
        TalentEditRequest editRequest = TalentEditRequest.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .skills(Set.of("Java", "Spring"))
                .build();

        TalentOwnProfileDTO expectedDto = new TalentOwnProfileDTO();
        expectedDto.setLastname(editRequest.getLastname());
        expectedDto.setFirstname(editRequest.getFirstname());
        expectedDto.setEmail(talent.getEmail());
        expectedDto.setBirthday(talent.getBirthday());
        expectedDto.setSkills(editRequest.getSkills());

        given(talentService.updateTalent(Mockito.anyLong(), Mockito.any(TalentEditRequest.class)))
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
    @DisplayName("[US-3] - Try edit someone else's profile")
    void tryEditSomeoneTalentProfile() throws Exception {
        TalentEditRequest editRequest = TalentEditRequest.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .skills(Set.of("Java", "Spring"))
                .build();

        given(talentService.updateTalent(Mockito.anyLong(), Mockito.any(TalentEditRequest.class)))
                .willThrow(new DeniedAccessException("You are not allowed to edit this talent"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{id}", talent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest))
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("You are not allowed to edit this talent"));
    }

    @Test
    @Order(12)
    @DisplayName("[US-3] - Fail editing own profile")
    void failEditingOwnProfile() throws Exception {
        TalentEditRequest editRequest = TalentEditRequest.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .build();

        given(talentService.updateTalent(Mockito.anyLong(), Mockito.any(TalentEditRequest.class)))
                .willThrow(NullPointerException.class);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{id}", talent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest))
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.skills")
                        .value("Empty skill list"));
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