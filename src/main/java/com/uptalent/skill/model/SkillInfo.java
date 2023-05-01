package com.uptalent.skill.model;

import com.uptalent.skill.model.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SkillInfo {
    private Long id;
    private String name;

    public SkillInfo(Skill skill) {
        this.id = skill.getId();
        this.name = skill.getName();
    }
}
