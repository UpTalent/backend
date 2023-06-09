package com.uptalent.proof.repository;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProofRepository extends JpaRepository<Proof, Long> {
    @Query("SELECT p " +
            "FROM proof p WHERE p.status = :contentStatus AND " +
            "coalesce((SELECT count(sk) FROM p.skillKudos sk WHERE sk.skill.name IN :skills GROUP BY p.id), 0) = :skillsSize")
    Page<Proof> findAllByStatus(ContentStatus contentStatus,
                                Pageable pageable,
                                String [] skills, int skillsSize);

    @Query("SELECT p, coalesce(sum(kh.totalKudos), 0) " +
            "FROM proof p LEFT JOIN kudos_history kh ON kh.proof.id = p.id AND kh.sponsor.id = :sponsorId " +
            "WHERE p.status = :contentStatus " +
            "GROUP BY p.id HAVING p.talent.id = :talentId")
    Page<Object[]> findAllTalentProofsBySponsorIdAndStatus(Long sponsorId,
                                                           Long talentId,
                                                           ContentStatus contentStatus, Pageable pageable);

    @Query("SELECT p, CASE WHEN (p.talent.id = :currentTalentId) THEN TRUE ELSE FALSE END " +
            "FROM proof p " +
            "WHERE p.status = :contentStatus AND p.talent.id = :talentId")
    Page<Object[]> findAllTalentProofsByTalentIdAndStatus(Long currentTalentId,
                                                          Long talentId,
                                                          ContentStatus contentStatus, Pageable pageable);

    @Query("SELECT p, coalesce(sum(kh.totalKudos), 0) " +
            "FROM proof p LEFT JOIN kudos_history kh ON kh.proof.id = p.id AND kh.sponsor.id = :sponsorId " +
            "WHERE p.status = :contentStatus " +
            "GROUP BY p.id " +
            "HAVING coalesce((SELECT count(sk) FROM p.skillKudos sk WHERE sk.skill.name IN :skills GROUP BY p.id), 0) = :skillsSize")
    Page<Object[]> findProofsAndKudosSumBySponsorId(Long sponsorId,
                                                    ContentStatus contentStatus,
                                                    Pageable pageable, String [] skills, int skillsSize);

    @Query("SELECT p, CASE WHEN (p.talent.id = :talentId) THEN TRUE ELSE FALSE END " +
            "FROM proof p " +
            "WHERE p.status = :contentStatus AND " +
            "coalesce((SELECT count(sk) FROM p.skillKudos sk WHERE sk.skill.name IN :skills GROUP BY p.id), 0) = :skillsSize")
    Page<Object[]> findProofsAndIsMyProofByTalentId(Long talentId,
                                                    ContentStatus contentStatus,
                                                    Pageable pageable, String[] skills, int skillsSize);

    @Query("select p from proof p join talent t on t.id = p.talent.id " +
            "where t.id = :talentId and p.status = 'PUBLISHED' order by p.kudos desc")
    Page<Proof> getMostKudosedProofByTalentId(Long talentId, Pageable pageable);

    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Query(value = "UPDATE proof p " +
            "SET p.status = com.uptalent.proof.model.enums.ContentStatus.HIDDEN " +
            "WHERE p.id IN :ids")
    void updateProofsDeleteData(List<Long> ids);
}