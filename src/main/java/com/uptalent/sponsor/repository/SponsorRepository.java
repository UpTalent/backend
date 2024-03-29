package com.uptalent.sponsor.repository;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.model.response.SponsorRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
    @Query("select new com.uptalent.proof.kudos.model.response.KudosedProof(kh.proof.id, " +
            "kh.proof.iconNumber, kh.proof.title, sum(kh.totalKudos), " +
            "new com.uptalent.util.model.response.Author(kh.proof.talent.id, " +
            "concat(kh.proof.talent.firstname, ' ', kh.proof.talent.lastname), " +
            "kh.proof.talent.avatar)) " +
            "from kudos_history kh " +
            "where kh.sponsor.id = :sponsorId and kh.proof.status = 'PUBLISHED' " +
            "group by kh.proof.id, kh.proof.iconNumber, kh.proof.title, " +
            "kh.proof.talent.id, kh.proof.talent.lastname, kh.proof.talent.firstname, " +
            "kh.proof.talent.avatar " +
            "order by sum(kh.totalKudos) desc ")
    Page<KudosedProof> findAllKudosedProofBySponsorId(Long sponsorId, PageRequest pageRequest);

    @Query("select kh " +
            "from kudos_history kh join kh.skillKudosHistories khsk join khsk.skill " +
            "where kh.sponsor.id = :sponsorId and kh.proof.id = :proofId and kh.proof.status = 'PUBLISHED' " +
            "order by kh.sent desc ")
    Page<KudosHistory> findAllKudosedProofHistoryBySponsorIdAndProofId(Long sponsorId,
                                                                       Long proofId,
                                                                       PageRequest pageRequest);

    @Query("SELECT s.avatar FROM sponsor s WHERE s.id = :sponsorId")
    Optional<String> findAvatarBySponsorId(Long sponsorId);

    @Query("SELECT new com.uptalent.sponsor.model.response.SponsorRating(s.fullname, s.avatar, SUM(kh.totalKudos)) " +
            "FROM kudos_history kh " +
            "JOIN kh.proof p " +
            "JOIN kh.sponsor s " +
            "WHERE p.talent.id = :talentId " +
            "GROUP BY s.id, s.fullname, s.avatar " +
            "ORDER BY SUM(kh.totalKudos) DESC")
    Page<SponsorRating> getSponsorRatingByTalentId(Long talentId, Pageable pageable);

    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Query(value = "UPDATE sponsor s " +
            "SET s.avatar=NULL," +
            "s.fullname='Deleted sponsor' " +
            "WHERE s.id IN :collect")
    void updateSponsorDeleteData(List<Long> collect);

    @Query("SELECT coalesce(SUM(kh.totalKudos), 0) " +
            "FROM kudos_history kh " +
            "JOIN kh.proof p " +
            "JOIN kh.sponsor s " +
            "WHERE s.id = :sponsorId and p.id = :proofId")
    Long sumKudosBySponsorAndProof(Long sponsorId, Long proofId);

}
