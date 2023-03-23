package com.uptalent.talent.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TalentProfileDTO {
    private Long id;
    private String lastname;
    private String firstname;
    private String photo;
    private String banner;
    private Set<String> skills;
    private String location;
    private String aboutMe;
}
