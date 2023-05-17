package com.uptalent.proof.repository;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProofRepository extends JpaRepository<Proof, Long> {
    @Query("SELECT p " +
            "FROM proof p WHERE p.status = :proofStatus AND " +
            "coalesce((SELECT count(sk) FROM p.skillKudos sk WHERE sk.skill.name IN :skills GROUP BY p.id), 0) = :skillsSize")
    Page<Proof> findAllByStatus(ProofStatus proofStatus,
                                Pageable pageable,
                                String [] skills, int skillsSize);

    @Query("SELECT p, coalesce(sum(kh.kudos), 0) " +
            "FROM proof p LEFT JOIN kudos_history kh ON kh.proof.id = p.id AND kh.sponsor.id = :sponsorId " +
            "WHERE p.status = :proofStatus " +
            "GROUP BY p.id HAVING p.talent.id = :talentId")
    Page<Object[]> findAllTalentProofsBySponsorIdAndStatus(Long sponsorId,
                                                           Long talentId,
                                                           ProofStatus proofStatus, Pageable pageable);

    @Query("SELECT p, CASE WHEN (p.talent.id = :currentTalentId) THEN TRUE ELSE FALSE END " +
            "FROM proof p " +
            "WHERE p.status = :proofStatus AND p.talent.id = :talentId")
    Page<Object[]> findAllTalentProofsByTalentIdAndStatus(Long currentTalentId,
                                                          Long talentId,
                                                          ProofStatus proofStatus, Pageable pageable);

    @Query("SELECT p, coalesce(sum(kh.kudos), 0) " +
            "FROM proof p LEFT JOIN kudos_history kh ON kh.proof.id = p.id AND kh.sponsor.id = :sponsorId " +
            "WHERE p.status = :proofStatus " +
            "GROUP BY p.id " +
            "HAVING coalesce((SELECT count(sk) FROM p.skillKudos sk WHERE sk.skill.name IN :skills GROUP BY p.id), 0) = :skillsSize")
    Page<Object[]> findProofsAndKudosSumBySponsorId(Long sponsorId,
                                                    ProofStatus proofStatus,
                                                    Pageable pageable, String [] skills, int skillsSize);

    @Query("SELECT p, CASE WHEN (p.talent.id = :talentId) THEN TRUE ELSE FALSE END " +
            "FROM proof p " +
            "WHERE p.status = :proofStatus AND " +
            "coalesce((SELECT count(sk) FROM p.skillKudos sk WHERE sk.skill.name IN :skills GROUP BY p.id), 0) = :skillsSize")
    Page<Object[]> findProofsAndIsMyProofByTalentId(Long talentId,
                                                    ProofStatus proofStatus,
                                                    Pageable pageable, String[] skills, int skillsSize);
}