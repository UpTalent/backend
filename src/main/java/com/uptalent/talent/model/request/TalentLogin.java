package com.uptalent.talent.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class TalentLogin {
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
