package com.uptalent.talent;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Talent {
    @Id
    @GeneratedValue
    private Long id;
    private String lastname;
    private String firstname;
    private String photo;
    private String banner;

    @ElementCollection
    private List<String> skills;

}
