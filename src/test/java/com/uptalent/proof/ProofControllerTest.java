package com.uptalent.proof;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.proof.controller.ProofController;
import com.uptalent.proof.exception.IllegalProofModifyingException;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.service.ProofService;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.service.TalentService;
import org.junit.jupiter.api.*;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    private ProofModify proofModify;
    private Proof publishedProof;
    private Proof hiddenProof;
    private Proof draftProof;
    private ProofModify editProofCase;
    private ProofModify publishProofCase;
    private ProofModify hideProofCase;
    private ProofModify reopenProofCase;

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
        draftProof = Proof.builder()
                .id(20L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .iconNumber(1)
                .status(ProofStatus.DRAFT)
                .talent(talent)
                .build();
        publishedProof = Proof.builder()
                .id(21L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(1)
                .status(ProofStatus.PUBLISHED)
                .talent(talent)
                .build();
        hiddenProof = Proof.builder()
                .id(22L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(1)
                .status(ProofStatus.HIDDEN)
                .talent(talent)
                .build();

        proofModify = new ProofModify("New Proof title", "New Proof summary", "New Proof content",
                2, ProofStatus.DRAFT.name());
        editProofCase = new ProofModify("Edit Proof title", "Edit Proof summary", "Edit Proof content",
                3, ProofStatus.DRAFT.name());
        publishProofCase = new ProofModify("Publish Proof title", "Publish Proof summary", "Publish Proof content",
                3, ProofStatus.PUBLISHED.name());
        hideProofCase = new ProofModify("Hide Proof title", "Hide Proof summary", "Hide Proof content",
                3, ProofStatus.HIDDEN.name());
        reopenProofCase = new ProofModify("Reopen Proof title", "Reopen Proof summary", "Reopen Proof content",
                3, ProofStatus.PUBLISHED.name());
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit not exist proof")
    public void editNotExistProof() throws Exception {
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new ProofNotFoundException("Proof not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), proof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proofModify)));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit proof other talent")
    public void editProofOtherTalent() throws Exception {
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new UnrelatedProofException("Proof not related to talent"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), proof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proofModify)));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit data in proof with draft status")
    public void editDataInProofWithDraftStatus() throws Exception {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(draftProof.getId())
                .title(editProofCase.getTitle())
                .summary(editProofCase.getSummary())
                .content(editProofCase.getContent())
                .iconNumber(editProofCase.getIconNumber())
                .published(draftProof.getPublished())
                .status(draftProof.getStatus())
                .build();

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willReturn(resultProof);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), proof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editProofCase)));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(editProofCase.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.summary").value(editProofCase.getSummary()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value(editProofCase.getContent()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.icon_number").value(editProofCase.getIconNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.published").value(draftProof.getPublished()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(draftProof.getStatus().toString()));
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit data in proof with other status")
    public void editDataInProofWithOtherStatus() throws Exception {
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalProofModifyingException("Illegal operation for modifying status"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), hiddenProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editProofCase)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - publish proof with draft status")
    public void publishProofWithDraftStatus() throws Exception {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(draftProof.getId())
                .title(draftProof.getTitle())
                .summary(draftProof.getSummary())
                .content(draftProof.getContent())
                .iconNumber(draftProof.getIconNumber())
                .published(LocalDateTime.now())
                .status(ProofStatus.PUBLISHED)
                .build();

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willReturn(resultProof);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), draftProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishProofCase)));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(ProofStatus.PUBLISHED.toString()));
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - publish proof with other status")
    public void publishProofWithOtherStatus() throws Exception {
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalProofModifyingException("Illegal operation for modifying status"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), publishedProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishProofCase)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - hide proof with published status")
    public void hideProofWithPublishedStatus() throws Exception {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(publishedProof.getId())
                .title(publishedProof.getTitle())
                .summary(publishedProof.getSummary())
                .content(publishedProof.getContent())
                .iconNumber(publishedProof.getIconNumber())
                .published(publishedProof.getPublished())
                .status(ProofStatus.HIDDEN)
                .build();

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willReturn(resultProof);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), publishedProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hideProofCase)));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(ProofStatus.HIDDEN.toString()));
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - hide proof with other status")
    public void hideProofWithOtherStatus() throws Exception {
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalProofModifyingException("Illegal operation for modifying status"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), hiddenProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hideProofCase)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - reopen proof with hidden status")
    public void reopenProofWithHiddenStatus() throws Exception {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(hiddenProof.getId())
                .title(hiddenProof.getTitle())
                .summary(hiddenProof.getSummary())
                .content(hiddenProof.getContent())
                .iconNumber(hiddenProof.getIconNumber())
                .published(hiddenProof.getPublished())
                .status(ProofStatus.PUBLISHED)
                .build();

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willReturn(resultProof);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), hiddenProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reopenProofCase)));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(ProofStatus.PUBLISHED.toString()));
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - reopen proof with other status")
    public void reopenProofWithOtherStatus() throws Exception {
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalProofModifyingException("Illegal operation for modifying status"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), publishedProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reopenProofCase)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Get proof detail info successfully")
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
    @DisplayName("[Stage-2] [US-6] - Try to get proof detail info when talent is not found")
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
    @DisplayName("[Stage-2] [US-6] - Try to get proof detail info when proof is not found")
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
    @DisplayName("[Stage-2] [US-6] - Try to get unrelated proof")
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
