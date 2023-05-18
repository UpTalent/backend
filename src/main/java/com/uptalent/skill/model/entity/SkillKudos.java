package com.uptalent.skill.model.entity;

import com.uptalent.proof.model.entity.Proof;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "skill_kudos")
@Table(name = "skill_kudos")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillKudos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "skill_id", referencedColumnName = "id")
    private Skill skill;
    @ManyToOne
    @JoinColumn(name = "proof_id", referencedColumnName = "id")
    private Proof proof;

    @Column(name = "kudos")
    private long kudos;

    @Override
    public String toString() {
        return "SkillKudos{" +
                "id=" + id +
                ", kudos=" + kudos +
                '}';
    }
}
