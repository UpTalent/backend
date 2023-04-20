package com.uptalent.talent.model.entity;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.model.entity.Proof;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

@Entity(name = "talent")
@Table(name = "talent")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Talent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 15, nullable = false, name = "lastname")
    private String lastname;
    @Column(length = 15, nullable = false, name = "firstname")
    private String firstname;
    @Column(name = "avatar")
    private String avatar;
    @Column(name = "banner")
    private String banner;

    @ElementCollection(fetch = EAGER)
    private Set<String> skills;

    @Column(name = "location")
    private String location;
    @Column(length = 100, nullable = false, name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "birthday")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Column(length = 2250, name = "about_me")
    private String aboutMe;

    @OneToMany(mappedBy = "talent",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Proof> proofs;

    @OneToMany(mappedBy = "talent")
    private Set<KudosHistory> kudosHistory;
}
