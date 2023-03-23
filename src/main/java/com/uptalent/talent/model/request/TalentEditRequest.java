package com.uptalent.talent.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TalentEditRequest {
    @NotBlank(message = "Blank lastname field")
    private String lastname;
    @NotBlank(message = "Blank firstname field")
    private String firstname;
    private Date birthday;
    @NotNull(message = "Empty skill list")
    private Set<String> skills;
    private String location;
    private String aboutMe;
}
