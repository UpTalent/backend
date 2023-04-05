package com.uptalent.proof;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.proof.controller.ProofController;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.service.ProofService;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.service.TalentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProofController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProofControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private ProofService proofService;


    @MockBean
    private TalentService talentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private Talent talent;
    private Proof proof;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long nonExistentProofId = 1000L;
    private static final Long nonExistentTalentId = 1000L;

    @BeforeEach
    public void setUp() {
        talent = Talent.builder()
                .id(1L)
                .lastname("Himonov")
                .firstname("Mark")
                .email("himonov.mark@gmail.com")
                .password("1234567890")
                .skills(Set.of("Java", "Spring"))
                .build();
        proof = Proof.builder()
                .id(1L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .status(ProofStatus.PUBLISHED)
                .talent(talent)
                .build();
    }

    @Test
    public void getProofDetailInfoSuccessfully() throws Exception {
        ProofDetailInfo mappedProof = ProofDetailInfo.builder()
                .id(proof.getId())
                .title(proof.getTitle())
                .summary(proof.getSummary())
                .content(proof.getContent())
                .published(proof.getPublished())
                .status(proof.getStatus())
                .build();

        given(proofService.getProofDetailInfo(talent.getId(), proof.getId()))
                .willReturn(mappedProof);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.talent").doesNotExist());
    }

    @Test
    public void getProofDetailInfoNotFound() throws Exception {
        given(proofService.getProofDetailInfo(nonExistentTalentId, proof.getId()))
                .willThrow(new TalentNotFoundException("Talent not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/proofs/{proofId}",
                                nonExistentTalentId, proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    public void getProofDetailInfoFromNonExistentProof() throws Exception {
        given(proofService.getProofDetailInfo(talent.getId(), nonExistentProofId))
                .willThrow(new ProofNotFoundException("Proof not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), nonExistentProofId)
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    public void getUnrelatedProof() throws Exception {
        Talent anotherTalent = Talent.builder()
                .id(2L)
                .lastname("Doe")
                .firstname("John")
                .email("doe.john@gmail.com")
                .password("1234567890")
                .skills(Set.of("Java", "Spring"))
                .build();

        Proof unrelatedProof = Proof.builder()
                .id(2L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .status(ProofStatus.PUBLISHED)
                .talent(anotherTalent)
                .build();

        given(proofService.getProofDetailInfo(talent.getId(), unrelatedProof.getId()))
                .willThrow(new UnrelatedProofException("This proof is not related to this talent's proofs"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), unrelatedProof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }
}
