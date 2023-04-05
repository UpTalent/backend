package com.uptalent.talent.model.entity;

import com.uptalent.proof.model.entity.Proof;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

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

    private String avatar;

    private String banner;

    @ElementCollection(fetch = EAGER)
    private Set<String> skills;

    private String location;

    private String email;

    private String password;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Column(length = 2250)
    private String aboutMe;

    @OneToMany(mappedBy = "talent",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Proof> proofs;
}
