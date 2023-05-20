package com.uptalent.proof.kudos.repository;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.response.KudosSender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KudosHistoryRepository extends JpaRepository<KudosHistory, Long> {
    @Query("select case when count(kh) > 0 then true else false end " +
            "from kudos_history kh where kh.sponsor.id = ?1 and kh.proof.id = ?2")
    Boolean pressedProofBySponsorId(Long sponsorId, Long proofId);

    @Query("SELECT kh " +
            "FROM kudos_history kh " +
            "JOIN kh.skillKudosHistories skh " +
            "JOIN skh.skill " +
            "WHERE kh.proof.id = :proofId")
    List<KudosHistory> findKudosSendersByProofId(Long proofId);

    @Query("select sum(kh.totalKudos) from kudos_history kh " +
            "group by kh.proof.id, kh.sponsor.id having kh.proof.id = :proofId and kh.sponsor.id = :sponsorId")
    Long sumKudosProofBySponsorId(Long sponsorId, Long proofId);
}
