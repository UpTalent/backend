package com.uptalent.talent.model.request;

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
public class TalentRegistrationRequest {
    @NotBlank(message = "Blank lastname field")
    String lastname;
    @NotBlank(message = "Blank firstname field")
    String firstname;
    @NotNull(message = "Empty skill list")
    Set<String> skills;
    @NotBlank(message = "Blank email field")
    String email;
    @NotBlank(message = "Blank password field")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password;
}
