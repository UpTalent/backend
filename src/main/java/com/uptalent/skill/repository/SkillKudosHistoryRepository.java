package com.uptalent.skill.repository;

import com.uptalent.skill.model.SkillInfo;
import com.uptalent.skill.model.entity.SkillKudosHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SkillKudosHistoryRepository extends JpaRepository<SkillKudosHistory, Long> {
    @Query("select new com.uptalent.skill.model.SkillInfo(sk.name, sum(skh.kudos)) from skill_kudos_history skh " +
            "join skh.skill sk " +
            "join skh.kudosHistory kh " +
            "where kh.sponsor.id = :sponsorId and proof.id = :proofId " +
            "group by sk.name")
    List<SkillInfo> findSumSkillsBySponsorIdAndProofId(Long sponsorId, Long proofId);
}
