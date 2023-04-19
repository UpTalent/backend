package com.uptalent.proof.kudos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KudosHistoryRepository extends JpaRepository<KudosHistory, Long> {
    @Query("select case when count(kh) > 0 then true else false end " +
            "from KudosHistory kh where kh.talent.id = ?1 and kh.proof.id = ?2")
    Boolean pressedProofByTalentId(Long talentId, Long proofId);
}
