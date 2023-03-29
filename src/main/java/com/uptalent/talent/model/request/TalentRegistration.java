package com.uptalent.talent.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Getter
@Setter
public class TalentRegistration {
    @NotBlank(message = "Lastname should not be blank")
    String lastname;
    @NotBlank(message = "Firstname should not be blank")
    String firstname;
    @NotNull(message = "Skills should not be null")
    Set<@NotBlank(message = "Name of skill should not be blank")
    @Size(max = 20, message = "Name of skill must be less than 20 characters") String> skills;
    @NotNull(message = "Email should not be null")
    @NotBlank(message = "Email should not be blank")
    @Email(message = "Email should be valid")
    String email;
    @NotNull(message = "Password should not be null")
    @NotBlank(message = "Password should not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password;
}
