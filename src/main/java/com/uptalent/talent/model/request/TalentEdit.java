package com.uptalent.talent.model.request;

import com.uptalent.skill.model.SkillTalentInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TalentEdit {
    @NotBlank(message = "Lastname should not be blank")
    @Size(max = 15, message = "Lastname must be less than 15 characters")
    private String lastname;

    @NotBlank(message = "Firstname should not be blank")
    @Size(max = 15, message = "Firstname must be less than 15 characters")
    private String firstname;

    private LocalDate birthday;


    private Set<SkillTalentInfo> skills;

    @Size(max = 255, message = "Location should be less than 255 characters")
    private String location;

    @Size(max = 2250, message = "About me should be less than 2250 characters")
    private String aboutMe;
}
