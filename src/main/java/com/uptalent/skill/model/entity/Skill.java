package com.uptalent.skill.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

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
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "skill_proof",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "proof_id")
    )
    private Set<Proof> proofs;
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "skill_talent",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "talent_id")
    )
    private Set<Talent> talents;
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
