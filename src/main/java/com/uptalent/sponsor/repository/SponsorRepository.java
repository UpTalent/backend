package com.uptalent.sponsor.repository;

import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.proof.kudos.model.response.KudosedProofHistory;
import com.uptalent.sponsor.model.entity.Sponsor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
    @Query("select new com.uptalent.proof.kudos.model.response.KudosedProof(kh.proof.id, " +
            "kh.proof.iconNumber, kh.proof.title, sum(kh.kudos)) " +
            "from kudos_history kh " +
            "where kh.sponsor.id = :sponsorId " +
            "group by kh.proof.id, kh.proof.iconNumber, kh.proof.title " +
            "order by sum(kh.kudos) desc ")
    Page<KudosedProof> findAllKudosedProofBySponsorId(Long sponsorId, PageRequest pageRequest);

    @Query("select new com.uptalent.proof.kudos.model.response.KudosedProofHistory(kh.sent, kh.kudos) " +
            "from kudos_history kh where kh.sponsor.id = :sponsorId and kh.proof.id = :proofId " +
            "order by kh.sent desc ")
    Page<KudosedProofHistory> findAllKudosedProofHistoryBySponsorIdAndProofId(Long sponsorId,
                                                                              Long proofId,
                                                                              PageRequest pageRequest);

    @Query("SELECT s.avatar FROM sponsor s WHERE s.id = :sponsorId")
    Optional<String> findAvatarBySponsorId(Long sponsorId);
}
