package com.uptalent.skill.model.entity;

import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity(name = "skill")
@Table(name = "skill")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false, name = "name")
    private String name;

    @OneToMany(mappedBy = "skill")
    private Set<SkillKudos> skillKudos;

    @ManyToMany
    @JoinTable(
            name = "skill_talent",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "talent_id")
    )
    private Set<Talent> talents;

    @OneToMany(mappedBy = "skill")
    private List<SkillKudosHistory> skillKudosHistories;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return Objects.equals(id, skill.id) && Objects.equals(name, skill.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
