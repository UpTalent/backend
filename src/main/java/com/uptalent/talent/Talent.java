package com.uptalent.talent;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;
import java.util.Set;

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
    private Set<String> skills;
    private String location;
    private String email;
    private String password;
    private Date birthday;
    private String aboutMe;

}
