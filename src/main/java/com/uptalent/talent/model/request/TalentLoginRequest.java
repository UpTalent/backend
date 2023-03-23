package com.uptalent.talent.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TalentLoginRequest(@Email String email,
                                 @NotNull @NotBlank String password) {
}
