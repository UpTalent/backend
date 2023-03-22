package com.uptalent.talent.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class TalentRegistrationRequest{
    @NotBlank(message = "Blank firstname field")
    String firstName;
    @NotBlank(message = "Blank lastname field")
    String lastName;
    @NotNull(message = "Empty skill list")
    List<String> skills;
    @NotBlank(message = "Blank email field")
    String email;
    @NotBlank(message = "Blank password field")
    String password;
}
