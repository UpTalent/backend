package com.uptalent.talent.model.response;

import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.skill.model.entity.Skill;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TalentProfile {
    private Long id;
    private String lastname;
    private String firstname;
    private String avatar;
    private String banner;
    private Set<SkillTalentInfo> skills;
    private String location;
    private String aboutMe;
}
