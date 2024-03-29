package com.uptalent.proof;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.credentials.repository.CredentialsRepository;
import com.uptalent.jwt.JwtTokenProvider;
import com.uptalent.proof.controller.ProofController;
import com.uptalent.proof.exception.*;
import com.uptalent.proof.kudos.model.request.PostKudos;
import com.uptalent.proof.kudos.model.request.PostKudosSkill;
import com.uptalent.proof.kudos.model.response.KudosSender;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.service.ProofService;
import com.uptalent.skill.exception.DuplicateSkillException;
import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.model.entity.SkillKudos;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.talent.service.TalentService;
import com.uptalent.util.exception.IllegalContentModifyingException;
import com.uptalent.util.exception.UnrelatedContentException;
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

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static com.uptalent.proof.model.enums.ContentStatus.HIDDEN;
import static com.uptalent.proof.model.enums.ContentStatus.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProofController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProofControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private TalentRepository talentRepository;

    @MockBean
    private ProofService proofService;


    @MockBean
    private TalentService talentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private CredentialsRepository credentialsRepository;
    @MockBean
    private SponsorRepository sponsorRepository;

    private Credentials credentials;
    private Talent talent;
    private Proof proof;

    @Autowired
    private ObjectMapper objectMapper;
    private Proof publishedProof;
    private Proof hiddenProof;
    private Proof draftProof;
    private Skill javaSkill;
    private Skill pythonSkill;
    private SkillKudos javaSkillKudos;
    private SkillKudos pythonSkillKudos;
    private SkillTalentInfo skillTalentInfo;
    private ProofModify proofModify;

    @BeforeEach
    public void setUp() {
        credentials = Credentials.builder()
                .id(1L)
                .email("himonov.mark@gmail.com")
                .password("1234567890")
                .status(AccountStatus.ACTIVE)
                .role(Role.TALENT)
                .build();
        talent = Talent.builder()
                .id(1L)
                .credentials(credentials)
                .lastname("Himonov")
                .firstname("Mark")
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
        draftProof = Proof.builder()
                .id(20L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .iconNumber(1)
                .status(ContentStatus.DRAFT)
                .talent(talent)
                .build();
        publishedProof = Proof.builder()
                .id(21L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(1)
                .status(ContentStatus.PUBLISHED)
                .talent(talent)
                .build();
        hiddenProof = Proof.builder()
                .id(22L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(1)
                .status(ContentStatus.HIDDEN)
                .talent(talent)
                .build();

        javaSkill = Skill.builder()
                .id(1L)
                .name("Java")
                .build();

        pythonSkill = Skill.builder()
                .id(2L)
                .name("Python")
                .build();

        javaSkillKudos = SkillKudos.builder()
                .id(1L)
                .skill(javaSkill)
                .build();

        pythonSkillKudos = SkillKudos.builder()
                .id(2L)
                .skill(pythonSkill)
                .build();

        proof.setSkillKudos(new HashSet<>(Arrays.asList(javaSkillKudos, pythonSkillKudos)));

        skillTalentInfo = new SkillTalentInfo(javaSkill.getId(), javaSkill.getName());
        proofModify = new ProofModify("Proof title",
                "Proof summary",
                "Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));

    }

    @Test
    @DisplayName("Create new proof successfully")
    void createProofSuccessfully() throws Exception {
        // given
        ProofModify proofModify = new ProofModify("Proof title",
                "Proof summary",
                "Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));
        URI proofLocation = new URI("http://mock/api/v1/talents/1/proofs/1");
        given(proofService.createProof(any(ProofModify.class), anyLong())).willReturn(proofLocation);

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents/{talentId}/proofs",
                        talent.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proofModify)));

        // then
        response
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", proofLocation.toString()));
    }

    @Test
    @DisplayName("Try to create proof in another talent profile")
    void createProofInAnotherTalentProfile() throws Exception {
        // given
        given(proofService.createProof(any(ProofModify.class), anyLong()))
                .willThrow(new DeniedAccessException("You do not have permission"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents/{talentId}/proofs",
                        talent.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proofModify)));

        // then
        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Try to create proof in non-existent talent profile")
    void createProofInNonExistentTalentProfile() throws Exception {
        // given
        given(proofService.createProof(any(ProofModify.class), anyLong()))
                .willThrow(new TalentNotFoundException("Talent not found"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents/{talentId}/proofs",
                        talent.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proofModify)));

        // then
        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Try to create proof with other status")
    void createProofWithOtherStatus() throws Exception {
        // given
        String errorMessage = "Proof status for creating should be DRAFT";

        given(proofService.createProof(any(ProofModify.class), anyLong()))
                .willThrow(new IllegalCreatingContentException(errorMessage));

        proofModify.setStatus(ContentStatus.PUBLISHED.name());

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents/{talentId}/proofs",
                                talent.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proofModify)));

        // then
        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Edit proof with other auth")
    void editProofWithOtherAuth() throws Exception {
        // given
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new DeniedAccessException("You do not have permission"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proofModify)));

        // then
        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Edit not exist proof")
    public void editNotExistProof() throws Exception {
        // given
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new ProofNotFoundException("Proof not found"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), proof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proofModify)));

        // then
        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Edit proof other talent")
    public void editProofOtherTalent() throws Exception {
        // given
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new UnrelatedContentException("Proof not related to talent"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), proof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(proofModify)));

        // when
        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Edit data in proof with draft status")
    public void editDataInProofWithDraftStatus() throws Exception {
        // given
        ProofModify editProofCase = new ProofModify("Edit Proof title",
                "Edit Proof summary",
                "Edit Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));

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

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), proof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editProofCase)));

        // then
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
    @DisplayName("Edit data in proof with other status")
    public void editDataInProofWithOtherStatus() throws Exception {
        // given
        ProofModify editProofCase = new ProofModify("Edit Proof title",
                "Edit Proof summary",
                "Edit Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalContentModifyingException("Illegal operation for modifying status"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), hiddenProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editProofCase)));

        // then
        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Publish proof with draft status")
    public void publishProofWithDraftStatus() throws Exception {
        // given
        ProofModify publishProofCase = new ProofModify(
                "Publish Proof title",
                "Publish Proof summary",
                "Publish Proof content",
                3,
                ContentStatus.PUBLISHED.name(),
                List.of(javaSkill.getId(), pythonSkill.getId()));
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(draftProof.getId())
                .title(draftProof.getTitle())
                .summary(draftProof.getSummary())
                .content(draftProof.getContent())
                .iconNumber(draftProof.getIconNumber())
                .published(LocalDateTime.now())
                .status(ContentStatus.PUBLISHED)
                .build();

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willReturn(resultProof);

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), draftProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishProofCase)));

        // then
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(ContentStatus.PUBLISHED.toString()));
    }

    @Test
    @DisplayName("Publish proof with other status")
    public void publishProofWithOtherStatus() throws Exception {
        // given
        ProofModify publishProofCase = new ProofModify(
                "Publish Proof title",
                "Publish Proof summary",
                "Publish Proof content",
                3,
                ContentStatus.PUBLISHED.name(),
                List.of(javaSkill.getId(), pythonSkill.getId()));
        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalContentModifyingException("Illegal operation for modifying status"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), publishedProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(publishProofCase)));

        // then
        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Hide proof with published status")
    public void hideProofWithPublishedStatus() throws Exception {
        // given
        ProofModify hideProofCase = new ProofModify("Hide Proof title",
                "Hide Proof summary",
                "Hide Proof content",
                3,
                HIDDEN.name(),
                List.of(javaSkill.getId()));
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(publishedProof.getId())
                .title(publishedProof.getTitle())
                .summary(publishedProof.getSummary())
                .content(publishedProof.getContent())
                .iconNumber(publishedProof.getIconNumber())
                .published(publishedProof.getPublished())
                .status(ContentStatus.HIDDEN)
                .build();

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willReturn(resultProof);

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), publishedProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hideProofCase)));

        // then
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(ContentStatus.HIDDEN.toString()));
    }

    @Test
    @DisplayName("Hide proof with other status")
    public void hideProofWithOtherStatus() throws Exception {
        // given
        ProofModify hideProofCase = new ProofModify("Hide Proof title",
                "Hide Proof summary",
                "Hide Proof content",
                3,
                HIDDEN.name(),
                List.of(javaSkill.getId()));

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalContentModifyingException("Illegal operation for modifying status"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), hiddenProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hideProofCase)));

        // then
        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Reopen proof with hidden status")
    public void reopenProofWithHiddenStatus() throws Exception {
        // given
        ProofModify reopenProofCase = new ProofModify("Reopen Proof title",
                "Reopen Proof summary",
                "Reopen Proof content",
                3,
                PUBLISHED.name(),
                List.of(javaSkill.getId()));

        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(hiddenProof.getId())
                .title(hiddenProof.getTitle())
                .summary(hiddenProof.getSummary())
                .content(hiddenProof.getContent())
                .iconNumber(hiddenProof.getIconNumber())
                .published(hiddenProof.getPublished())
                .status(ContentStatus.PUBLISHED)
                .build();

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willReturn(resultProof);

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), hiddenProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reopenProofCase)));

        // then
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(ContentStatus.PUBLISHED.toString()));
    }

    @Test
    @DisplayName("Reopen proof with other status")
    public void reopenProofWithOtherStatus() throws Exception {
        // given
        ProofModify reopenProofCase = new ProofModify("Reopen Proof title",
                "Reopen Proof summary",
                "Reopen Proof content",
                3,
                PUBLISHED.name(),
                List.of(javaSkill.getId()));

        given(proofService.editProof(any(ProofModify.class), anyLong(), anyLong()))
                .willThrow(new IllegalContentModifyingException("Illegal operation for modifying status"));

        // when
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{talentId}/proofs/{proofId}",
                        talent.getId(), publishedProof.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reopenProofCase)));

        // then
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
        given(proofService.getProofDetailInfo(talent.getId(), proof.getId()))
                .willThrow(new TalentNotFoundException("Talent not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try to get proof detail info when proof is not found")
    public void getProofDetailInfoFromNonExistentProof() throws Exception {
        given(proofService.getProofDetailInfo(talent.getId(), proof.getId()))
                .willThrow(new ProofNotFoundException("Proof not found"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
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

                .build();

        Proof unrelatedProof = Proof.builder()
                .id(2L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .status(ContentStatus.PUBLISHED)
                .talent(anotherTalent)
                .build();

        given(proofService.getProofDetailInfo(talent.getId(), unrelatedProof.getId()))
                .willThrow(new UnrelatedContentException("This proof is not related to this talent's proofs"));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), unrelatedProof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Delete proof successfully")
    public void deleteProofSuccessfully() throws Exception {
        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try delete someone else's proof")
    public void tryDeleteSomeoneProof() throws Exception {
        willThrow(new DeniedAccessException("You are not allowed to delete this proof"))
                .given(proofService).deleteProof(proof.getId(), talent.getId());


        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try to delete proof when talent is not found")
    public void tryDeleteProofWhenTalentNotFound() throws Exception {
        willThrow(new TalentNotFoundException("Talent not found"))
                .given(proofService).deleteProof(proof.getId(), proof.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try to delete non-existent proof")
    public void tryDeleteNonExistentProof() throws Exception {
        willThrow(new ProofNotFoundException("Proof not found"))
                .given(proofService).deleteProof(proof.getId(), talent.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{talentId}/proofs/{proofId}",
                                talent.getId(), proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }



    @Test
    @DisplayName("[Stage-3.2] [US-1] - post kudos successfully as sponsor")
    public void postKudosSuccessfullyAsSponsor() throws Exception {
        PostKudos postKudos = generatePostKudos();

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/proofs/{proofId}/kudos",
                                proof.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postKudos)));

        response
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("[Stage-3.2] [US-1] - post kudos to proof which has not status PUBLISHED")
    public void postKudosToProofWhichHasNotStatusPublished() throws Exception {
        PostKudos postKudos = generatePostKudos();

        String errorMessage = "Proof was not found";

        willThrow(new ProofNotFoundException(errorMessage)).given(proofService).postKudos(any(PostKudos.class), anyLong());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/proofs/{proofId}/kudos",
                                draftProof.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postKudos)));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Try to post kudos with duplicate skills")
    public void tryPostKudosWithDuplicateSkills() throws Exception {
        PostKudos postKudos = generatePostKudos();
        postKudos.getPostKudosSkills().add(postKudos.getPostKudosSkills().get(0));

        willThrow(new DuplicateSkillException("Duplicate skills"))
                .given(proofService).postKudos(any(PostKudos.class), anyLong());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/proofs/{proofId}/kudos",
                                proof.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postKudos)));

        response
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[Stage-3.1] [US-3] - Get kudos senders successfully")
    public void getKudosSendersSuccessfully() throws Exception {
        Talent anotherTalent = Talent.builder()
                .id(2L)
                .lastname("Doe")
                .firstname("John")

                .build();

        List<KudosSender> expectedKudosSenders = List.of(
                KudosSender.builder()
                        .fullname(talent.getFirstname())
                        .avatar(talent.getAvatar())
                        .sent(LocalDateTime.now())
                        .kudos(1)
                        .build(),
                KudosSender.builder()
                        .fullname(anotherTalent.getFirstname())
                        .avatar(anotherTalent.getAvatar())
                        .sent(LocalDateTime.now())
                        .kudos(1)
                        .build()
        );
        given(proofService.getKudosSenders(proof.getId()))
                .willReturn(expectedKudosSenders);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/proofs/{proofId}/kudos",
                                proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk());
        String responseBody = response.andReturn().getResponse().getContentAsString();

        assertThat(responseBody).isEqualTo(objectMapper.writeValueAsString(expectedKudosSenders));
    }

    @Test
    @DisplayName("[Stage-3.1] [US-3] - Try to get kudos senders from non-existent proof")
    public void tryGetKudosSendersFromNonExistentProof() throws Exception {
        willThrow(new ProofNotFoundException("Proof not found"))
                .given(proofService).getKudosSenders(proof.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/proofs/{proofId}/kudos",
                                proof.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    private PostKudos generatePostKudos() {
        List<PostKudosSkill> postKudosSkills = new ArrayList<>(List.of(
                new PostKudosSkill(255L, javaSkill.getId()),
                new PostKudosSkill(25L, pythonSkill.getId())));
        return new PostKudos(postKudosSkills);
    }
}
