package com.uptalent.talent.model.response;

import com.uptalent.skill.model.SkillTalentInfo;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentGeneralInfo {
    private Long id;
    private String lastname;
    private String firstname;
    private String avatar;
    private String banner;
    private Set<SkillTalentInfo> skills;
}
