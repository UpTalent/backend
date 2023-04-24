package com.uptalent.sponsor.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SponsorEdit {
    @NotBlank(message = "Full name should not be blank")
    @Size(max = 30, message = "Full name must be less than 30 characters")
    private String fullname;

}
