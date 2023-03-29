package com.uptalent.talent.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TalentEdit {
    @NotBlank(message = "Blank lastname field")
    private String lastname;
    @NotBlank(message = "Blank firstname field")
    private String firstname;
    private LocalDate birthday;
    @NotNull(message = "Empty skill list")
    private Set<@NotBlank(message = "Name of skill should not be blank")
    @Size(max = 20, message = "Name of skill must be less than 20 characters") String> skills;
    private String location;
    private String aboutMe;
}
