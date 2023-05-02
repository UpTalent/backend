package com.uptalent.mapper;

import com.uptalent.skill.model.SkillInfo;
import com.uptalent.skill.model.entity.Skill;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    List<SkillInfo> toSkillInfos(List<Skill> skills);
}
