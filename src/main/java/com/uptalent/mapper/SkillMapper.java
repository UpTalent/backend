package com.uptalent.mapper;

import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.skill.model.entity.Skill;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    List<SkillTalentInfo> toSkillInfos(List<Skill> skills);
}
