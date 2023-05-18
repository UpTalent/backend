package com.uptalent.mapper;

import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TalentMapper {
    default List<TalentGeneralInfo> toTalentGeneralInfos(List<Talent> talents){
        return talents.stream()
                .map(this::toTalentGeneralInfo)
                .collect(Collectors.toList());
    }

    default TalentGeneralInfo toTalentGeneralInfo(Talent talent){
        return new TalentGeneralInfo(
                talent.getId(),
                talent.getLastname(),
                talent.getFirstname(),
                talent.getAvatar(),
                talent.getBanner(),
                talent.getSkills().stream()
                        .map(skill -> new SkillTalentInfo(skill.getId(), skill.getName()))
                        .collect(Collectors.toSet())
        );
    }

    default TalentProfile toTalentProfile(Talent talent){
        return new TalentProfile(
                talent.getId(),
                talent.getLastname(),
                talent.getFirstname(),
                talent.getAvatar(),
                talent.getBanner(),
                talent.getSkills().stream()
                        .map(skill -> new SkillTalentInfo(skill.getId(), skill.getName()))
                        .collect(Collectors.toSet()),
                talent.getLocation(),
                talent.getAboutMe()
        );
    }
    default TalentOwnProfile toTalentOwnProfile(Talent talent){
        return new TalentOwnProfile(
                talent.getId(),
                talent.getLastname(),
                talent.getFirstname(),
                talent.getAvatar(),
                talent.getBanner(),
                talent.getSkills().stream()
                        .map(skill -> new SkillTalentInfo(skill.getId(), skill.getName()))
                        .collect(Collectors.toSet()),
                talent.getLocation(),
                talent.getAboutMe(),
                talent.getCredentials().getEmail(),
                talent.getBirthday()
        );
    }
}
