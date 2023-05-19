package com.uptalent.skill.model.entity;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "skill_kudos_history")
@Table(name = "skill_kudos_history")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillKudosHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "skill_id", referencedColumnName = "id")
    private Skill skill;

    @ManyToOne
    @JoinColumn(name = "kudos_history_id", referencedColumnName = "id")
    private KudosHistory kudosHistory;

    private Long kudos;
}
