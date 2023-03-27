package com.uptalent.talent.model.request;

import jakarta.validation.constraints.*;

public record TalentLoginRequest(
        @NotBlank
        @Email
        String email,
        @NotNull
        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {}
