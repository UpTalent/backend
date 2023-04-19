package com.uptalent.proof.kudos.repository;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.response.KudosSender;
import com.uptalent.talent.model.entity.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KudosHistoryRepository extends JpaRepository<KudosHistory, Long> {
    @Query("select case when count(kh) > 0 then true else false end " +
            "from KudosHistory kh where kh.talent.id = ?1 and kh.proof.id = ?2")
    Boolean pressedProofByTalentId(Long talentId, Long proofId);

    @Query("SELECT new com.uptalent.proof.kudos.model.response.KudosSender" +
            "(kh.talent.lastname, kh.talent.firstname, kh.talent.avatar, kh.sent, kh.kudos) " +
            "FROM KudosHistory kh WHERE kh.proof.id = :proofId")
    List<KudosSender> findKudosSendersByProofId(Long proofId);
}
