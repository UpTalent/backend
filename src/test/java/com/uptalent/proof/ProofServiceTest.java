package com.uptalent.proof;

import com.uptalent.mapper.ProofMapper;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.entity.ProofStatus;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.proof.service.ProofService;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import static org.mockito.Mockito.when;

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
                .status(ProofStatus.PUBLISHED)
                .talent(talent)
                .build();
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
        Talent anotherTalent = Talent.builder()
                .id(2L)
                .lastname("Doe")
                .firstname("John")
                .email("doe.john@gmail.com")
                .password(passwordEncoder.encode("1234567890"))
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
        // given
        given(proofRepository.findById(unrelatedProof.getId())).willReturn(Optional.of(unrelatedProof));

        // when
        when(talentRepository.existsById(talent.getId())).thenReturn(true);

        // then
        assertThrows(UnrelatedProofException.class,
                () -> proofService.getProofDetailInfo(talent.getId(), unrelatedProof.getId()));
    }
}
