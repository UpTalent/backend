package com.uptalent.sponsor.repository;

import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.sponsor.model.entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
    @Query("select new com.uptalent.proof.kudos.model.response.KudosedProof(kh.proof.id, kh.proof.iconNumber," +
            "kh.proof.title, kh.sent, kh.kudos) " +
            "from kudos_history kh where kh.sponsor.id = :sponsorId")
    List<KudosedProof> findAllKudosedProofBySponsorId(Long sponsorId);
}
