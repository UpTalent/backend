package com.uptalent.talent.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TalentEditRequest {
    @NotBlank(message = "Blank lastname field")
    private String lastname;
    @NotBlank(message = "Blank firstname field")
    private String firstname;
    private LocalDate birthday;
    @NotNull(message = "Empty skill list")
    private Set<@NotBlank(message = "Name of skill should not be blank") String> skills;
    private String location;
    private String aboutMe;
}
