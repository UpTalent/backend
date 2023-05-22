package com.uptalent.skill.repository;

import com.uptalent.skill.model.SkillInfo;
import com.uptalent.skill.model.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface SkillRepository extends JpaRepository<Skill, Long> {
    @Query("select new com.uptalent.skill.model.SkillInfo(sk.name, sum(sks.kudos)) from skill sk " +
            "join skill_kudos sks on sk.id = sks.skill.id " +
            "join proof p on p.id = sks.proof.id " +
            "join talent t on t.id = p.talent.id where t.id = :talentId " +
            "group by sk.name " +
            "order by sum(sks.kudos) desc")
    Page<SkillInfo> getMostKudosedSkillsByTalentId(Long talentId, Pageable pageable);
}
