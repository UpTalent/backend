package com.uptalent.proof.repository;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProofRepository extends JpaRepository<Proof, Long> {
    @Query("SELECT new com.uptalent.proof.model.response.ProofGeneralInfo(p.id, p.iconNumber, p.title, p.summary, p.kudos, p.published) " +
            "FROM proof p " +
            "WHERE p.status = :proofStatus")
    Page<ProofGeneralInfo> findAllByStatus(ProofStatus proofStatus, PageRequest pageable);

    @Query("SELECT new com.uptalent.proof.model.response.ProofSponsorDetailInfo(p.id, p.iconNumber, p.title, p.summary, " +
            "p.content, p.published, p.kudos, p.status, coalesce(sum(kh.kudos), 0)) " +
            "FROM proof p LEFT JOIN kudos_history kh ON kh.proof.id = p.id AND kh.sponsor.id = :sponsorId " +
            "WHERE p.status = :proofStatus " +
            "GROUP BY p.id having p.talent.id = :talentId")
    Page<ProofSponsorDetailInfo> findAllTalentProofsBySponsorIdAndStatus(Long sponsorId, Long talentId, ProofStatus proofStatus, PageRequest of);

    @Query("SELECT new com.uptalent.proof.model.response.ProofTalentDetailInfo(p.id, p.iconNumber, p.title, p.summary, " +
            "p.content, p.published, p.kudos, p.status, " +
            "CASE WHEN (p.talent.id = :currentTalentId)  THEN TRUE ELSE FALSE END) " +
            "FROM proof p" +
            " WHERE p.status = :proofStatus and p.talent.id = :talentId")
    Page<ProofTalentDetailInfo> findAllTalentProofsByTalentIdAndStatus(Long currentTalentId, Long talentId, ProofStatus proofStatus, PageRequest of);

    @Query("SELECT new com.uptalent.proof.model.response.ProofSponsorGeneralInfo(p.id, p.iconNumber, p.title, p.summary, p.kudos, p.published, " +
            "coalesce(sum(kh.kudos), 0)) " +
            "FROM proof p LEFT JOIN kudos_history kh ON kh.proof.id = p.id AND kh.sponsor.id = :sponsorId " +
            "WHERE p.status = :proofStatus " +
            "GROUP BY p.id")
    Page<ProofSponsorGeneralInfo> findAllWithKudosedBySponsorId(Long sponsorId,
                                                                ProofStatus proofStatus,
                                                                Pageable pageable);

    @Query("SELECT new com.uptalent.proof.model.response.ProofTalentGeneralInfo(p.id, p.iconNumber, p.title, p.summary, p.kudos, p.published, " +
            "CASE WHEN (p.talent.id = :talentId)  THEN TRUE ELSE FALSE END) " +
            "FROM proof p" +
            " WHERE p.status = :proofStatus group by p.id")
    Page<ProofTalentGeneralInfo> findAllWithTalentProofByTalentId(Long talentId,
                                                                  ProofStatus proofStatus,
                                                                  Pageable pageable);

}
