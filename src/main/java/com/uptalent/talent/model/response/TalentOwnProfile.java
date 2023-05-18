package com.uptalent.talent.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.skill.model.SkillTalentInfo;
import com.uptalent.skill.model.entity.Skill;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TalentOwnProfile extends TalentProfile {
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    public TalentOwnProfile(Long id,
                            String lastname,
                            String firstname,
                            String avatar,
                            String banner,
                            Set<SkillTalentInfo> skills,
                            String location,
                            String aboutMe,
                            String email,
                            LocalDate birthday) {
        super(id, lastname, firstname, avatar, banner, skills, location, aboutMe);
        this.email = email;
        this.birthday = birthday;
    }
}
