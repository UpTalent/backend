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

    @Query("SELECT new com.uptalent.proof.kudos.model.response.KudosSender" +
            "(kh.sponsor.fullname, kh.sponsor.avatar, kh.sent, kh.kudos) " +
            "FROM kudos_history kh WHERE kh.proof.id = :proofId")
    List<KudosSender> findKudosSendersByProofId(Long proofId);

    @Query("select sum(kh.kudos) from kudos_history kh " +
            "group by kh.proof.id, kh.sponsor.id having kh.proof.id = :proofId and kh.sponsor.id = :sponsorId")
    Integer sumKudosProofBySponsorId(Long sponsorId, Long proofId);
}
