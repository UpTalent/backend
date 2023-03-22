package com.uptalent.talent.model.res;

import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TalentDTO {
    private Long id;
    private String lastname;
    private String firstname;
    private String photo;
    private String banner;
    private Set<String> skills;
}
