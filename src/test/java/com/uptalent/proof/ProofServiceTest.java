package com.uptalent.proof;

import com.uptalent.mapper.ProofMapper;
import com.uptalent.proof.exception.IllegalProofModifyingException;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.proof.service.ProofService;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProofServiceTest {
    @Mock
    private ProofRepository proofRepository;

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private ProofMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProofService proofService;

    private static final Long nonExistentProofId = 1000L;
    private static final Long nonExistentTalentId = 1000L;

    private Proof proof;
    private Talent talent;
    private Talent anotherTalent;
    private ProofModify proofModify;
    private Proof publishedProof;
    private Proof hiddenProof;
    private Proof drartProof;
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
                .password(passwordEncoder.encode("1234567890"))
                .skills(Set.of("Java", "Spring"))
                .build();

        proof = Proof.builder()
                .id(1L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(1)
                .status(ProofStatus.PUBLISHED)
                .talent(talent)
                .build();

        drartProof = Proof.builder()
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

        anotherTalent = Talent.builder()
                .id(2L)
                .lastname("Doe")
                .firstname("John")
                .email("doe.john@gmail.com")
                .password(passwordEncoder.encode("1234567890"))
                .skills(Set.of("Java", "Spring"))
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
    public void getProofDetailInfoSuccessfully() {
        // given
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));

        ProofDetailInfo mappedProof = ProofDetailInfo.builder()
                .id(proof.getId())
                .title(proof.getTitle())
                .summary(proof.getSummary())
                .content(proof.getContent())
                .published(proof.getPublished())
                .status(proof.getStatus())
                .build();

        // when
        when(talentRepository.existsById(talent.getId())).thenReturn(true);
        when(mapper.toProofDetailInfo(proof)).thenReturn(mappedProof);

        ProofDetailInfo proofDetailInfo = proofService.getProofDetailInfo(talent.getId(),
                proof.getId());

        // then
        assertThat(proofDetailInfo).isNotNull();
        assertThat(proofDetailInfo.getId()).isEqualTo(proof.getId());
        assertThat(proofDetailInfo.getTitle()).isEqualTo(proof.getTitle());
        assertThat(proofDetailInfo.getSummary()).isEqualTo(proof.getSummary());
        assertThat(proofDetailInfo.getContent()).isEqualTo(proof.getContent());
        assertThat(proofDetailInfo.getPublished()).isEqualTo(proof.getPublished());
        assertThat(proofDetailInfo.getStatus()).isEqualTo(proof.getStatus());
    }

    @Test
    public void getProofDetailInfoFromNonExistentTalent() {
        // when
        when(talentRepository.existsById(nonExistentTalentId)).thenReturn(false);

        // then
        assertThrows(TalentNotFoundException.class,
                () -> proofService.getProofDetailInfo(nonExistentTalentId, proof.getId()));
    }

    @Test
    public void getProofDetailInfoFromNonExistentProof() {
        // when
        when(talentRepository.existsById(talent.getId())).thenReturn(true);
        when(proofRepository.findById(nonExistentProofId)).thenReturn(Optional.empty());

        // then
        assertThrows(ProofNotFoundException.class,
                () -> proofService.getProofDetailInfo(talent.getId(), nonExistentProofId));
    }

    @Test
    public void getUnrelatedProof() {
        Proof unrelatedProof = Proof.builder()
                .id(2L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(2)
                .status(ProofStatus.PUBLISHED)
                .talent(anotherTalent)
                .build();
        // given
        given(proofRepository.findById(unrelatedProof.getId())).willReturn(Optional.of(unrelatedProof));

        // when
        when(talentRepository.existsById(talent.getId())).thenReturn(true);

        // then
        assertThrows(UnrelatedProofException.class,
                () -> proofService.getProofDetailInfo(talent.getId(), unrelatedProof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit not exist proof")
    void updateNotExistsProof() {
        given(proofRepository.findById(proof.getId())).willReturn(Optional.empty());

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(ProofNotFoundException.class,
                () -> proofService.editProof(proofModify, talent.getId(), proof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit proof other talent")
    void updateProofOtherTalent() {
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));

        given(talentRepository.existsById(anotherTalent.getId())).willReturn(true);

        assertThrows(UnrelatedProofException.class,
                () -> proofService.editProof(proofModify, anotherTalent.getId(), proof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit data in proof with draft status")
    void updateDataInProofWithDraftStatus() {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(drartProof.getId())
                .title(editProofCase.getTitle())
                .summary(editProofCase.getSummary())
                .content(editProofCase.getContent())
                .iconNumber(editProofCase.getIconNumber())
                .published(drartProof.getPublished())
                .status(drartProof.getStatus())
                .build();

        given(proofRepository.findById(drartProof.getId()))
                .willReturn(Optional.of(drartProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        doReturn(resultProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(editProofCase, talent.getId(), drartProof.getId());

        assertThat(editProofCase.getTitle()).isEqualTo(proofDetailInfo.getTitle());
        assertThat(editProofCase.getSummary()).isEqualTo(proofDetailInfo.getSummary());
        assertThat(editProofCase.getContent()).isEqualTo(proofDetailInfo.getContent());
        assertThat(editProofCase.getIconNumber()).isEqualTo(proofDetailInfo.getIconNumber());
    }

    @Test
    @DisplayName("[Stage-2] [US-4] - edit data in proof with other status")
    void updateDataInProofWithOtherStatus() {
        given(proofRepository.findById(hiddenProof.getId()))
                .willReturn(Optional.of(hiddenProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalProofModifyingException.class,
                () -> proofService.editProof(editProofCase, talent.getId(), hiddenProof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - publish proof with draft status")
    void publishProofWithDraftStatus() {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(drartProof.getId())
                .title(drartProof.getTitle())
                .summary(drartProof.getSummary())
                .content(drartProof.getContent())
                .iconNumber(drartProof.getIconNumber())
                .published(LocalDateTime.now())
                .status(ProofStatus.PUBLISHED)
                .build();

        given(proofRepository.findById(drartProof.getId()))
                .willReturn(Optional.of(drartProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        doReturn(resultProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(publishProofCase, talent.getId(), drartProof.getId());

        assertThat(proofDetailInfo.getPublished()).isNotNull();
        assertThat(proofDetailInfo.getStatus()).isEqualTo(ProofStatus.PUBLISHED);
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - publish proof with other status")
    void publishProofWithOtherStatus() {
        given(proofRepository.findById(publishedProof.getId()))
                .willReturn(Optional.of(publishedProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalProofModifyingException.class,
                () -> proofService.editProof(publishProofCase, talent.getId(), publishedProof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - hide proof with published status")
    void hideProofWithPublishedStatus() {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(publishedProof.getId())
                .title(publishedProof.getTitle())
                .summary(publishedProof.getSummary())
                .content(publishedProof.getContent())
                .iconNumber(publishedProof.getIconNumber())
                .published(publishedProof.getPublished())
                .status(ProofStatus.HIDDEN)
                .build();

        given(proofRepository.findById(publishedProof.getId()))
                .willReturn(Optional.of(publishedProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        doReturn(resultProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(hideProofCase, talent.getId(), publishedProof.getId());

        assertThat(proofDetailInfo.getStatus()).isEqualTo(ProofStatus.HIDDEN);
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - hide proof with other status")
    void hideProofWithOtherStatus() {
        given(proofRepository.findById(drartProof.getId()))
                .willReturn(Optional.of(drartProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalProofModifyingException.class,
                () -> proofService.editProof(hideProofCase, talent.getId(), drartProof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - reopen proof with hidden status")
    void reopenProofWithHiddenStatus() {
        ProofDetailInfo resultProof = ProofDetailInfo.builder()
                .id(hiddenProof.getId())
                .title(hiddenProof.getTitle())
                .summary(hiddenProof.getSummary())
                .content(hiddenProof.getContent())
                .iconNumber(hiddenProof.getIconNumber())
                .published(hiddenProof.getPublished())
                .status(ProofStatus.PUBLISHED)
                .build();

        given(proofRepository.findById(hiddenProof.getId()))
                .willReturn(Optional.of(hiddenProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        doReturn(resultProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(reopenProofCase, talent.getId(), hiddenProof.getId());

        assertThat(proofDetailInfo.getStatus()).isEqualTo(ProofStatus.PUBLISHED);
    }

    @Test
    @DisplayName("[Stage-2] [US-5] - reopen proof with other status")
    void reopenProofWithOtherStatus() {
        given(proofRepository.findById(publishedProof.getId()))
                .willReturn(Optional.of(publishedProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalProofModifyingException.class,
                () -> proofService.editProof(reopenProofCase, talent.getId(), publishedProof.getId()));
    }
}
