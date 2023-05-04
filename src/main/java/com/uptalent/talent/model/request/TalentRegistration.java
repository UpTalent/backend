package com.uptalent.talent.model.request;

import com.uptalent.skill.model.SkillInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TalentRegistration {
    @NotBlank(message = "Lastname should not be blank")
    @Size(max = 15, message = "Lastname must be less than 15 characters")
    private String lastname;

    @NotBlank(message = "Firstname should not be blank")
    @Size(max = 15, message = "Firstname must be less than 15 characters")
    private String firstname;


    private Set<SkillInfo> skills;

    @NotNull(message = "Email should not be null")
    @NotBlank(message = "Email should not be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @NotNull(message = "Password should not be null")
    @NotBlank(message = "Password should not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Size(max = 32, message = "Password must be less than 32 characters")
    private String password;
}
