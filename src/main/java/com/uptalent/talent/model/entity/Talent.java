package com.uptalent.talent.model.entity;


import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.proof.model.entity.Proof;

import com.uptalent.skill.model.entity.Skill;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;



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

    @OneToOne
    @JoinTable(
            name = "talent_credentials",
            joinColumns = @JoinColumn(name = "talent_id"),
            inverseJoinColumns = @JoinColumn(name = "credentials_id")
    )
    private Credentials credentials;

    @Column(length = 15, nullable = false, name = "lastname")
    private String lastname;

    @Column(length = 15, nullable = false, name = "firstname")
    private String firstname;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "banner")
    private String banner;


    @ManyToMany(mappedBy = "talents")
    private Set<Skill> skills;

    @Column(name = "location")
    private String location;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Talent talent = (Talent) o;
        return Objects.equals(id, talent.id) && Objects.equals(lastname, talent.lastname) && Objects.equals(firstname, talent.firstname) && Objects.equals(avatar, talent.avatar) && Objects.equals(banner, talent.banner) && Objects.equals(location, talent.location) && Objects.equals(birthday, talent.birthday) && Objects.equals(aboutMe, talent.aboutMe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastname, firstname, avatar, banner, location, birthday, aboutMe);
    }
}
