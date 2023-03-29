package com.uptalent.talent.model.request;

import jakarta.validation.constraints.*;

public record TalentLogin(
        @NotNull(message = "Email should not be null")
        @NotBlank(message = "Email should not be blank")
        @Email(message = "Email should be valid")
        String email,
        @NotNull(message = "Password should not be null")
        @NotBlank(message = "Password should not be blank")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {}
