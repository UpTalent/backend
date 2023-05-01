package com.uptalent.skill.repository;

import com.uptalent.skill.model.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}
