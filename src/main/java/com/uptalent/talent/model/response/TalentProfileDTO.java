package com.uptalent.talent.model.response;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
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
