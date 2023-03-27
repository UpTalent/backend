package com.uptalent.talent.model.request;

import jakarta.validation.constraints.*;

public record TalentLoginRequest(
        @NotBlank
        @Email
        String email,
        @NotNull
        @NotBlank
        @Size(min = 5, message = "Password must be at least 5 characters long")
        String password
) {}
