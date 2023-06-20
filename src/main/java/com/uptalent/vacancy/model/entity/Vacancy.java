package com.uptalent.vacancy.model.entity;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.model.entity.SkillKudos;
import com.uptalent.sponsor.model.entity.Sponsor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity(name = "vacancy")
@Table(name = "vacancy")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "title")
    private String title;

    @Column(length = 3000, nullable = false, name = "content")
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "published")
    private LocalDateTime published;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ContentStatus status;

    @ManyToOne
    @JoinColumn(name = "sponsor_id", referencedColumnName = "id")
    private Sponsor sponsor;

    @Column(name = "count_matched_skills", nullable = false)
    private Integer countMatchedSkills;

    @ManyToMany
    @JoinTable(
            name = "skill_vacancy",
            joinColumns = @JoinColumn(name = "vacancy_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills;
}
