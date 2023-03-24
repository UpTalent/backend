package com.uptalent.talent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.UpTalentBackendApplication;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.mapper.TalentMapper;
import com.uptalent.talent.TalentRepository;
import com.uptalent.talent.TalentService;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.exception.DeniedAccessException;
import com.uptalent.talent.model.exception.TalentNotFoundException;
import com.uptalent.talent.model.response.TalentOwnProfileDTO;
import com.uptalent.talent.model.response.TalentProfileDTO;
import lombok.SneakyThrows;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TalentControllerTest {
    @InjectMocks
    TalentController talentController;

    @Mock
    TalentService talentService;

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
    void getTalentProfileSuccessfully() {
        Long id = 3L;
        given(talentService.getTalentProfileById(id))
                .willReturn(new TalentProfileDTO());

        ResponseEntity<Object> responseEntity = ResponseEntity
                .of(Optional.of(talentController.getTalentProfile(id)));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @Order(3)
    @DisplayName("[US-2] - Get own profile successfully")
    void getOwnProfileSuccessfully() {
        given(talentService.getTalentProfileById(talent.getId()))
                .willReturn(new TalentOwnProfileDTO(talent.getEmail(), new Date()));

        ResponseEntity<Object> responseEntity = ResponseEntity
                .of(Optional.of(talentController.getTalentProfile(talent.getId())));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @Order(4)
    @DisplayName("[US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() {
        Long nonExistentTalentId = 1000L;
        given(talentService.getTalentProfileById(nonExistentTalentId))
                .willThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentController.getTalentProfile(nonExistentTalentId));
    }

    @Test
    @Order(13)
    @DisplayName("[US-4] - Delete own profile successfully")
    void deleteOwnProfileSuccessfully() {
        willDoNothing().given(talentService).deleteTalent(talent.getId());

        ResponseEntity<?> responseEntity = talentController.deleteTalent(talent.getId());


        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    @Order(14)
    @DisplayName("[US-4] - Try delete someone else's profile")
    void tryDeleteSomeoneTalentProfile() {
        willThrow(new DeniedAccessException("You are not allowed to delete this talent"))
                .given(talentService).deleteTalent(talent.getId());

        assertThrows(DeniedAccessException.class, () -> talentController.deleteTalent(talent.getId()));
    }

    @Test
    @Order(15)
    @DisplayName("[US-4] - Delete non-existent profile")
    void deleteNonExistentProfile() {
        Long nonExistentTalentId = 1000L;
        willThrow(new TalentNotFoundException("Talent was not found"))
                .given(talentService).deleteTalent(nonExistentTalentId);

        assertThrows(TalentNotFoundException.class, () -> talentController.deleteTalent(nonExistentTalentId));
    }
}