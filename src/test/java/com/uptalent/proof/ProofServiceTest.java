package com.uptalent.proof;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.mapper.ProofMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.proof.kudos.exception.IllegalPostingKudos;
import com.uptalent.proof.kudos.model.response.KudosSender;
import com.uptalent.proof.kudos.repository.KudosHistoryRepository;
import com.uptalent.proof.kudos.model.request.PostKudos;
import com.uptalent.skill.model.SkillInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.util.service.AccessVerifyService;
import org.springframework.data.domain.Page;
import com.uptalent.proof.exception.IllegalProofModifyingException;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofGeneralInfo;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled
public class ProofServiceTest {
    @Mock
    private ProofRepository proofRepository;

    @Mock
    private TalentRepository talentRepository;
    @Mock
    private KudosHistoryRepository kudosHistoryRepository;

    @Mock
    private ProofMapper mapper;
    @Mock
    private AccessVerifyService accessVerifyService;

    @Mock
    private SponsorRepository sponsorRepository;


    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProofService proofService;

    private Proof proof;
    private Credentials credentials;
    private Talent talent;
    private Talent anotherTalent;
    private ProofModify proofModify;
    private Proof publishedProof;
    private Proof hiddenProof;
    private Proof draftProof;
    private ProofModify editProofCase;
    private ProofModify publishProofCase;
    private ProofModify hideProofCase;
    private ProofModify reopenProofCase;

    private Sponsor sponsor;
    private Skill skill;
    private SkillInfo skillInfo;


    @BeforeEach
    public void setUp() {
        credentials = Credentials.builder()
                .id(1L)
                .email("himonov.mark@gmail.com")
                .password(passwordEncoder.encode("1234567890"))
                .status(AccountStatus.ACTIVE)
                .role(Role.TALENT)
                .build();
        talent = Talent.builder()
                .id(1L)
                .credentials(credentials)
                .lastname("Himonov")
                .firstname("Mark")
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

        anotherTalent = Talent.builder()
                .id(2L)
                .lastname("Doe")
                .firstname("John")
                .skills(Set.of("Java", "Spring"))
                .build();

        sponsor = Sponsor.builder()
                .id(1L)
                .fullname("SoftServe")
                .kudos(50)
                .build();


        skill = Skill.builder()
                .id(1L)
                .name("Java")
                .build();

        skillInfo = new SkillInfo(skill.getId(), skill.getName());

        talent.setProofs(new ArrayList<>(Arrays.asList(draftProof, publishedProof, hiddenProof)));

        proofModify = new ProofModify("New Proof title", "New Proof summary", "New Proof content",
                2, ProofStatus.DRAFT.name(), Set.of(skillInfo));

        editProofCase = new ProofModify("Edit Proof title", "Edit Proof summary", "Edit Proof content",
                3, ProofStatus.DRAFT.name(), Set.of(skillInfo));
        publishProofCase = new ProofModify("Publish Proof title", "Publish Proof summary", "Publish Proof content",
                3, ProofStatus.PUBLISHED.name(), Set.of(skillInfo));
        hideProofCase = new ProofModify("Hide Proof title", "Hide Proof summary", "Hide Proof content",
                3, ProofStatus.HIDDEN.name(), Set.of(skillInfo));
        reopenProofCase = new ProofModify("Reopen Proof title", "Reopen Proof summary", "Reopen Proof content",
                3, ProofStatus.PUBLISHED.name(), Set.of(skillInfo));
    }

/*
    @Test
    @DisplayName("[Stage 2] [US 1-2] - Get all proofs successfully in service")
    public void getProofGeneralInfoSuccessfully() {

        List<ProofGeneralInfo> proofGeneralInfos = List.of(
                ProofGeneralInfo.builder()
                        .id(proof.getId())
                        .title(proof.getTitle())
                        .iconNumber(proof.getIconNumber())
                        .published(proof.getPublished())
                        .summary(proof.getSummary())
                        .build(),
                ProofGeneralInfo.builder()
                        .id(publishedProof.getId())
                        .title(publishedProof.getTitle())
                        .summary(publishedProof.getSummary())
                        .iconNumber(publishedProof.getIconNumber())
                        .published(publishedProof.getPublished())
                        .build()
        );

        Page<ProofGeneralInfo> proofsPage = new PageImpl<>(proofGeneralInfos);

        when(proofRepository.findAllByStatus(ProofStatus.PUBLISHED, PageRequest.of(0, 9, Sort.by("published").descending())))
                .thenReturn(proofsPage);

        PageWithMetadata<? extends ProofGeneralInfo> result = proofService.getProofs(0, 9, "desc");

        verify(proofRepository, times(1)).findAllByStatus(ProofStatus.PUBLISHED, PageRequest.of(0, 9, Sort.by("published").descending()));

        assertThat(result.getContent()).isEqualTo(proofGeneralInfos);
        assertThat(result.getContent().get(0).getPublished()).isEqualTo(proofGeneralInfos.get(0).getPublished());
    }
*/
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
                .id(draftProof.getId())
                .title(editProofCase.getTitle())
                .summary(editProofCase.getSummary())
                .content(editProofCase.getContent())
                .iconNumber(editProofCase.getIconNumber())
                .published(draftProof.getPublished())
                .status(draftProof.getStatus())
                .build();

        given(proofRepository.findById(draftProof.getId()))
                .willReturn(Optional.of(draftProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        doReturn(resultProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(editProofCase, talent.getId(), draftProof.getId());

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
                .id(draftProof.getId())
                .title(draftProof.getTitle())
                .summary(draftProof.getSummary())
                .content(draftProof.getContent())
                .iconNumber(draftProof.getIconNumber())
                .published(LocalDateTime.now())
                .status(ProofStatus.PUBLISHED)
                .build();

        given(proofRepository.findById(draftProof.getId()))
                .willReturn(Optional.of(draftProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        doReturn(resultProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(publishProofCase, talent.getId(), draftProof.getId());

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
        given(proofRepository.findById(draftProof.getId()))
                .willReturn(Optional.of(draftProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalProofModifyingException.class,
                () -> proofService.editProof(hideProofCase, talent.getId(), draftProof.getId()));
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

    @Test
    @DisplayName("[Stage-2] [US-6] - Get proof detail info successfully")
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
    @DisplayName("[Stage-2] [US-6] - Try to get proof detail info when talent is not found")
    public void getProofDetailInfoFromNonExistentTalent() {
        // when
        when(talentRepository.existsById(proof.getId())).thenReturn(false);

        // then
        assertThrows(TalentNotFoundException.class,
                () -> proofService.getProofDetailInfo(proof.getId(), proof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try to get proof detail info when proof is not found")
    public void getProofDetailInfoFromNonExistentProof() {
        // when
        when(talentRepository.existsById(talent.getId())).thenReturn(true);
        when(proofRepository.findById(proof.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(ProofNotFoundException.class,
                () -> proofService.getProofDetailInfo(talent.getId(), proof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try to get unrelated proof")
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

        given(proofRepository.findById(unrelatedProof.getId())).willReturn(Optional.of(unrelatedProof));

        when(talentRepository.existsById(talent.getId())).thenReturn(true);

        assertThrows(UnrelatedProofException.class,
                () -> proofService.getProofDetailInfo(talent.getId(), unrelatedProof.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Delete proof successfully")
    public void deleteProofSuccessfully() {
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(talentRepository.existsById(talent.getId())).willReturn(true);

        proofService.deleteProof(proof.getId(), talent.getId());

        verify(proofRepository).delete(proof);

        assertThat(proofRepository.existsById(proof.getId())).isFalse();
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try delete someone else's proof")
    public void tryDeleteSomeoneProof() {
        given(talentRepository.existsById(talent.getId())).willReturn(true);

        willThrow(DeniedAccessException.class)
                .given(accessVerifyService)
                .tryGetAccess(
                        talent.getId(), talent.getCredentials().getRole(),
                        "You do not have permission to delete proof"
                );


        assertThrows(DeniedAccessException.class,
                () -> proofService.deleteProof(proof.getId(), talent.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try to delete proof when talent is not found")
    public void tryDeleteProofWhenTalentNotFound() {
        given(talentRepository.existsById(talent.getId())).willReturn(false);

        assertThrows(TalentNotFoundException.class,
                () -> proofService.deleteProof(proof.getId(), talent.getId()));
    }

    @Test
    @DisplayName("[Stage-2] [US-6] - Try to delete non-existent proof")
    public void tryDeleteNonExistentProof() {
        given(talentRepository.existsById(talent.getId())).willReturn(true);
        given(proofRepository.findById(proof.getId())).willReturn(Optional.empty());

        assertThrows(ProofNotFoundException.class,
                () -> proofService.deleteProof(proof.getId(), talent.getId()));
    }


    @Test
    @DisplayName("[Stage-3.2] [US-2] - post kudos successfully as sponsor")
    public void postKudosSuccessfullyAsSponsor() {
        PostKudos postKudos = new PostKudos(1);
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        int balanceKudosBeforePosting = sponsor.getKudos();
        int countKudosProofBeforePosting = proof.getKudos();

        proofService.postKudos(postKudos, proof.getId());
        Assertions.assertEquals(countKudosProofBeforePosting + postKudos.getKudos(), proof.getKudos());
        Assertions.assertEquals(balanceKudosBeforePosting - postKudos.getKudos(), sponsor.getKudos());
    }

    @Test
    @DisplayName("[Stage-3.2] [US-2] - post kudos with negative balance")
    public void postKudosToOwnProof() {
        PostKudos postKudos = new PostKudos(1);
        sponsor.setKudos(-50);

        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        assertThrows(IllegalPostingKudos.class,
                () -> proofService.postKudos(postKudos, proof.getId()));
    }



    @Test
    @DisplayName("[Stage-3.2] [US-2] - post kudos to proof which has not status PUBLISHED")
    public void postKudosToProofWhichHasNotStatusPublished() {
        PostKudos postKudos = new PostKudos(1);

        given(proofRepository.findById(draftProof.getId())).willReturn(Optional.of(draftProof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        assertThrows(ProofNotFoundException.class,
                () -> proofService.postKudos(postKudos, draftProof.getId()));
    }

    @Test
    @DisplayName("[Stage-3.1] [US-3] - Get kudos senders successfully")
    public void getKudosSendersSuccessfully() {
        given(accessVerifyService.getPrincipalId()).willReturn(proof.getTalent().getId());
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(proofRepository.existsById(proof.getId())).willReturn(true);

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

        when(kudosHistoryRepository.findKudosSendersByProofId(proof.getId())).thenReturn(expectedKudosSenders);
        List<KudosSender> actualKudosSenders = proofService.getKudosSenders(proof.getId());

        verify(proofRepository).existsById(proof.getId());
        verify(kudosHistoryRepository).findKudosSendersByProofId(proof.getId());

        assertThat(expectedKudosSenders.size()).isEqualTo(actualKudosSenders.size());

        for (int i = 0; i < expectedKudosSenders.size(); i++) {
            KudosSender expected = expectedKudosSenders.get(i);
            KudosSender actual = actualKudosSenders.get(i);
            assertThat(expected.getFullname()).isEqualTo(actual.getFullname());
            assertThat(expected.getAvatar()).isEqualTo(actual.getAvatar());
            assertThat(expected.getSent()).isEqualTo(actual.getSent());
            assertThat(expected.getKudos()).isEqualTo(actual.getKudos());
        }
    }

    @Test
    @DisplayName("[Stage-3.1] [US-3] - Try to get kudos senders from non-existent proof")
    public void tryGetKudosSendersFromNonExistentProof() {
        given(proofRepository.existsById(proof.getId())).willReturn(false);

        assertThrows(ProofNotFoundException.class,
                () -> proofService.getKudosSenders(proof.getId()));
    }
}
