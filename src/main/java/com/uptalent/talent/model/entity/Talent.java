package com.uptalent.talent.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String aboutMe;

}
