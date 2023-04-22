package com.uptalent.sponsor.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SponsorRegistration {
    @NotBlank(message = "Full name should not be blank")
    @Size(max = 30, message = "Full name must be less than 15 characters")
    private String fullname;

    @NotBlank(message = "Email should not be blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @NotBlank(message = "Password should not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Size(max = 32, message = "Password must be less than 32 characters")
    private String password;
}
