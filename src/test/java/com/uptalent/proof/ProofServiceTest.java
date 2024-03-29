package com.uptalent.proof;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.mapper.ProofMapper;
import com.uptalent.proof.kudos.exception.IllegalPostingKudos;
import com.uptalent.proof.kudos.model.request.PostKudosSkill;
import com.uptalent.proof.kudos.model.response.UpdatedProofKudos;
import com.uptalent.proof.kudos.repository.KudosHistoryRepository;
import com.uptalent.proof.kudos.model.request.PostKudos;
import com.uptalent.skill.exception.DuplicateSkillException;
import com.uptalent.skill.model.SkillProofInfo;
import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.model.entity.SkillKudos;
import com.uptalent.skill.repository.SkillKudosHistoryRepository;
import com.uptalent.skill.repository.SkillKudosRepository;
import com.uptalent.skill.repository.SkillRepository;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.util.exception.IllegalContentModifyingException;
import com.uptalent.util.exception.UnrelatedContentException;
import com.uptalent.util.service.AccessVerifyService;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ContentStatus;
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
import java.util.*;

import static com.uptalent.proof.model.enums.ContentStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith({MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProofServiceTest {
    @Mock
    private ProofRepository proofRepository;

    @Mock
    private TalentRepository talentRepository;
    @Mock
    private KudosHistoryRepository kudosHistoryRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillKudosRepository skillKudosRepository;
    @Mock
    private SkillKudosHistoryRepository skillKudosHistoryRepository;

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
    private Proof publishedProof;
    private Proof hiddenProof;
    private Proof draftProof;
    private Sponsor sponsor;
    private Skill javaSkill;
    private Skill pythonSkill;
    private SkillKudos javaSkillKudos;
    private SkillKudos pythonSkillKudos;
    private SkillTalentInfo skillTalentInfo;

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
                .build();

        proof = Proof.builder()
                .id(1L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .published(LocalDateTime.now())
                .iconNumber(1)
                .status(ContentStatus.PUBLISHED)
                .talent(talent)
                .kudos(0)
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
                .status(HIDDEN)
                .talent(talent)
                .build();

        anotherTalent = Talent.builder()
                .id(2L)
                .lastname("Doe")
                .firstname("John")
                .build();

        sponsor = Sponsor.builder()
                .id(1L)
                .fullname("SoftServe")
                .kudos(500)
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

        draftProof = Proof.builder()
                .id(20L)
                .title("Proof title")
                .summary("Proof summary")
                .content("Proof content")
                .iconNumber(1)
                .status(DRAFT)
                .talent(talent)
                .build();

        proof.setSkillKudos(new HashSet<>(Arrays.asList(javaSkillKudos, pythonSkillKudos)));

        skillTalentInfo = new SkillTalentInfo(javaSkill.getId(), javaSkill.getName());

        talent.setProofs(new ArrayList<>(Arrays.asList(draftProof, publishedProof, hiddenProof)));

    }


    @Test
    @DisplayName("Edit not exist proof")
    void updateNotExistsProof() {
        ProofModify proofModify = new ProofModify("Proof title",
                "Proof summary",
                "Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));

        given(proofRepository.findById(proof.getId())).willReturn(Optional.empty());

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(ProofNotFoundException.class,
                () -> proofService.editProof(proofModify, talent.getId(), proof.getId()));
    }

    @Test
    @DisplayName("Edit proof other talent")
    void updateProofOtherTalent() {
        ProofModify proofModify = new ProofModify("Proof title",
                "Proof summary",
                "Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));

        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));

        given(talentRepository.existsById(anotherTalent.getId())).willReturn(true);

        assertThrows(UnrelatedContentException.class,
                () -> proofService.editProof(proofModify, anotherTalent.getId(), proof.getId()));
    }

    @Test
    @DisplayName("Edit data in proof with draft status")
    void updateDataInProofWithDraftStatus() {
        // given
        ProofModify editProofCase = new ProofModify("Edit Proof title",
                "Edit Proof summary",
                "Edit Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));

        ProofDetailInfo editProof = ProofDetailInfo.builder()
                .id(draftProof.getId())
                .title(editProofCase.getTitle())
                .summary(editProofCase.getSummary())
                .content(editProofCase.getContent())
                .iconNumber(editProofCase.getIconNumber())
                .status(DRAFT)
                .skills(Set.of(new SkillProofInfo(javaSkill.getId(), javaSkill.getName(), javaSkillKudos.getKudos())))
                .build();

        given(proofRepository.findById(draftProof.getId()))
                .willReturn(Optional.of(draftProof));
        given(talentRepository.existsById(talent.getId())).willReturn(true);
        given(skillRepository.findAllById(editProofCase.getSkillIds())).willReturn(List.of(javaSkill));

        // when
        doReturn(editProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(editProofCase,
                talent.getId(),
                draftProof.getId());

        // then
        assertThat(editProofCase.getTitle()).isEqualTo(proofDetailInfo.getTitle());
        assertThat(editProofCase.getSummary()).isEqualTo(proofDetailInfo.getSummary());
        assertThat(editProofCase.getContent()).isEqualTo(proofDetailInfo.getContent());
        assertThat(editProofCase.getIconNumber()).isEqualTo(proofDetailInfo.getIconNumber());
        assertTrue(proofDetailInfo.getSkills().stream()
                .map(SkillProofInfo::getName)
                .toList()
                .contains(javaSkill.getName()));
    }

    @Test
    @DisplayName("Edit data in proof with other status")
    void updateDataInProofWithOtherStatus() {
        ProofModify editProofCase = new ProofModify("Edit Proof title",
                "Edit Proof summary",
                "Edit Proof content",
                3,
                ContentStatus.DRAFT.name(),
                List.of(javaSkill.getId()));

        given(proofRepository.findById(hiddenProof.getId()))
                .willReturn(Optional.of(hiddenProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalContentModifyingException.class,
                () -> proofService.editProof(editProofCase, talent.getId(), hiddenProof.getId()));
    }

    @Test
    @DisplayName("Publish proof with draft status")
    void publishProofWithDraftStatus() {
        // given
        ProofModify publishProofCase = new ProofModify(
                "Publish Proof title",
                "Publish Proof summary",
                "Publish Proof content",
                3,
                ContentStatus.PUBLISHED.name(),
                List.of(javaSkill.getId(), pythonSkill.getId()));
        ProofDetailInfo publishProof = ProofDetailInfo.builder()
                .id(draftProof.getId())
                .title(publishProofCase.getTitle())
                .summary(publishProofCase.getSummary())
                .content(publishProofCase.getContent())
                .iconNumber(publishProofCase.getIconNumber())
                .status(PUBLISHED)
                .published(LocalDateTime.now())
                .skills(Set.of(
                        new SkillProofInfo(javaSkill.getId(), javaSkill.getName(), javaSkillKudos.getKudos()),
                        new SkillProofInfo(pythonSkill.getId(), pythonSkill.getName(), pythonSkillKudos.getKudos())))
                .build();

        given(proofRepository.findById(draftProof.getId()))
                .willReturn(Optional.of(draftProof));
        given(talentRepository.existsById(talent.getId())).willReturn(true);
        given(skillRepository.findAllById(publishProofCase.getSkillIds())).willReturn(List.of(javaSkill, pythonSkill));

        // when
        doReturn(publishProof).when(mapper).toProofDetailInfo(any(Proof.class));
        ProofDetailInfo proofDetailInfo = proofService.editProof(publishProofCase, talent.getId(), draftProof.getId());

        // then
        assertThat(proofDetailInfo.getStatus()).isEqualTo(PUBLISHED);
        assertThat(publishProofCase.getTitle()).isEqualTo(proofDetailInfo.getTitle());
        assertThat(publishProofCase.getSummary()).isEqualTo(proofDetailInfo.getSummary());
        assertThat(publishProofCase.getContent()).isEqualTo(proofDetailInfo.getContent());
        assertThat(publishProofCase.getIconNumber()).isEqualTo(proofDetailInfo.getIconNumber());
        assertTrue(proofDetailInfo.getSkills().stream()
                .map(SkillProofInfo::getName)
                .toList()
                .contains(javaSkill.getName()));
        assertTrue(proofDetailInfo.getSkills().stream()
                .map(SkillProofInfo::getName)
                .toList()
                .contains(pythonSkill.getName()));
        assertNotNull(proofDetailInfo.getPublished());
    }

    @Test
    @DisplayName("Publish proof with other status")
    void publishProofWithOtherStatus() {
        ProofModify publishProofCase = new ProofModify(
                "Publish Proof title",
                "Publish Proof summary",
                "Publish Proof content",
                3,
                ContentStatus.PUBLISHED.name(),
                List.of(javaSkill.getId(), pythonSkill.getId()));

        given(proofRepository.findById(publishedProof.getId()))
                .willReturn(Optional.of(publishedProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalContentModifyingException.class,
                () -> proofService.editProof(publishProofCase, talent.getId(), publishedProof.getId()));
    }

    @Test
    @DisplayName("Hide proof with published status")
    void hideProofWithPublishedStatus() {
        // given
        ProofModify hideProofCase = new ProofModify("Hide Proof title",
                "Hide Proof summary",
                "Hide Proof content",
                3,
                HIDDEN.name(),
                List.of(javaSkill.getId()));

        ProofDetailInfo hiddenProof = ProofDetailInfo.builder()
                .id(publishedProof.getId())
                .title(hideProofCase.getTitle())
                .summary(hideProofCase.getSummary())
                .content(hideProofCase.getContent())
                .iconNumber(hideProofCase.getIconNumber())
                .published(publishedProof.getPublished())
                .status(HIDDEN)
                .skills(Set.of(new SkillProofInfo(javaSkill.getId(), javaSkill.getName(), javaSkillKudos.getKudos())))
                .build();

        given(proofRepository.findById(publishedProof.getId()))
                .willReturn(Optional.of(publishedProof));
        given(talentRepository.existsById(talent.getId())).willReturn(true);

        // when
        doReturn(hiddenProof).when(mapper).toProofDetailInfo(any(Proof.class));
        ProofDetailInfo proofDetailInfo = proofService.editProof(hideProofCase, talent.getId(), publishedProof.getId());

        // then
        assertThat(proofDetailInfo.getStatus()).isEqualTo(HIDDEN);
    }

    @Test
    @DisplayName("Hide proof with other status")
    void hideProofWithOtherStatus() {
        ProofModify hideProofCase = new ProofModify("Hide Proof title",
                "Hide Proof summary",
                "Hide Proof content",
                3,
                HIDDEN.name(),
                List.of(javaSkill.getId()));

        given(proofRepository.findById(draftProof.getId()))
                .willReturn(Optional.of(draftProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalContentModifyingException.class,
                () -> proofService.editProof(hideProofCase, talent.getId(), draftProof.getId()));
    }

    @Test
    @DisplayName("Reopen proof with hidden status")
    void reopenProofWithHiddenStatus() {
        // given
        ProofModify reopenProofCase = new ProofModify("Reopen Proof title",
                "Reopen Proof summary",
                "Reopen Proof content",
                3,
                PUBLISHED.name(),
                List.of(javaSkill.getId()));

        ProofDetailInfo reopenedProof = ProofDetailInfo.builder()
                .id(hiddenProof.getId())
                .title(hiddenProof.getTitle())
                .summary(hiddenProof.getSummary())
                .content(hiddenProof.getContent())
                .iconNumber(hiddenProof.getIconNumber())
                .published(hiddenProof.getPublished())
                .status(PUBLISHED)
                .skills(Set.of(new SkillProofInfo(javaSkill.getId(), javaSkill.getName(), javaSkillKudos.getKudos())))
                .build();

        given(proofRepository.findById(hiddenProof.getId()))
                .willReturn(Optional.of(hiddenProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        // when
        doReturn(reopenedProof).when(mapper).toProofDetailInfo(any(Proof.class));

        ProofDetailInfo proofDetailInfo = proofService.editProof(reopenProofCase, talent.getId(), hiddenProof.getId());

        // then
        assertThat(proofDetailInfo.getStatus()).isEqualTo(PUBLISHED);
        assertThat(proofDetailInfo.getId()).isEqualTo(reopenedProof.getId());
    }

    @Test
    @DisplayName("Reopen proof with other status")
    void reopenProofWithOtherStatus() {
        ProofModify reopenProofCase = new ProofModify("Reopen Proof title",
                "Reopen Proof summary",
                "Reopen Proof content",
                3,
                PUBLISHED.name(),
                List.of(javaSkill.getId()));

        given(proofRepository.findById(publishedProof.getId()))
                .willReturn(Optional.of(publishedProof));

        given(talentRepository.existsById(talent.getId())).willReturn(true);

        assertThrows(IllegalContentModifyingException.class,
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
                .status(ContentStatus.PUBLISHED)
                .talent(anotherTalent)
                .build();

        given(proofRepository.findById(unrelatedProof.getId())).willReturn(Optional.of(unrelatedProof));

        when(talentRepository.existsById(talent.getId())).thenReturn(true);

        assertThrows(UnrelatedContentException.class,
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
        PostKudos postKudos = generatePostKudos();
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        given(skillRepository.findAllById(List.of(javaSkill.getId(), pythonSkill.getId())))
                .willReturn(List.of(javaSkill, pythonSkill));

        long balanceKudosBeforePosting = sponsor.getKudos();
        long countKudosProofBeforePosting = proof.getKudos();

        long expectedKudosSum = postKudos.getPostKudosSkills().stream()
                .mapToLong(PostKudosSkill::getKudos)
                .sum();

        UpdatedProofKudos result = proofService.postKudos(postKudos, proof.getId());
        List<SkillProofInfo> skillProofInfos = result.getSkills();

        assertThat(postKudos.getPostKudosSkills().size()).isEqualTo(skillProofInfos.size());
        assertThat(skillProofInfos.get(0).getId()).isEqualTo(postKudos.getPostKudosSkills().get(0).getSkillId());
        assertThat(skillProofInfos.get(0).getKudos()).isEqualTo(postKudos.getPostKudosSkills().get(0).getKudos());
        assertThat(countKudosProofBeforePosting + expectedKudosSum).isEqualTo(proof.getKudos());
        assertThat(balanceKudosBeforePosting - expectedKudosSum).isEqualTo(sponsor.getKudos());
    }

    @Test
    @DisplayName("Try to post kudos when sponsor's balance is less than post kudos")
    public void tryPostKudosWhenSponsorBalanceIsLessThanPostKudos() {
        PostKudos postKudos = generatePostKudos();
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        postKudos.getPostKudosSkills().get(0).setKudos(sponsor.getKudos());

        assertThrows(IllegalPostingKudos.class,
                () -> proofService.postKudos(postKudos, proof.getId()));
    }

    @Test
    @DisplayName("[Stage-3.2] [US-2] - post kudos with negative balance")
    public void postKudosToOwnProof() {
        PostKudos postKudos = generatePostKudos();
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        sponsor.setKudos(-1);

        assertThrows(IllegalPostingKudos.class,
                () -> proofService.postKudos(postKudos, proof.getId()));
    }



    @Test
    @DisplayName("[Stage-3.2] [US-2] - post kudos to proof which has not status PUBLISHED")
    public void postKudosToProofWhichHasNotStatusPublished() {
        PostKudos postKudos = generatePostKudos();
        given(proofRepository.findById(draftProof.getId())).willReturn(Optional.of(draftProof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        assertThrows(ProofNotFoundException.class,
                () -> proofService.postKudos(postKudos, draftProof.getId()));
    }

    @Test
    @DisplayName("Try to post kudos with duplicate skills")
    public void tryPostKudosWithDuplicateSkills() {
        PostKudos postKudos = generatePostKudos();
        given(proofRepository.findById(proof.getId())).willReturn(Optional.of(proof));
        given(sponsorRepository.findById(sponsor.getId())).willReturn(Optional.of(sponsor));
        given(accessVerifyService.getPrincipalId()).willReturn(sponsor.getId());

        postKudos.getPostKudosSkills().add(postKudos.getPostKudosSkills().get(0));

        assertThrows(DuplicateSkillException.class,
                () -> proofService.postKudos(postKudos, proof.getId()));
    }

    /*
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
*/
    @Test
    @DisplayName("[Stage-3.1] [US-3] - Try to get kudos senders from non-existent proof")
    public void tryGetKudosSendersFromNonExistentProof() {
        given(proofRepository.existsById(proof.getId())).willReturn(false);

        assertThrows(ProofNotFoundException.class,
                () -> proofService.getKudosSenders(proof.getId()));
    }

    private PostKudos generatePostKudos() {
        List<PostKudosSkill> postKudosSkills = new ArrayList<>(List.of(
                new PostKudosSkill(25L, javaSkill.getId()),
                new PostKudosSkill(25L, pythonSkill.getId())));
        return new PostKudos(postKudosSkills);
    }
}
