package com.uptalent.proof.model.entity;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.skill.model.entity.SkillKudos;
import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Entity(name = "proof")
@Table(name = "proof")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proof {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "icon_number")
    private Integer iconNumber;

    @Column(nullable = false, name = "title")
    private String title;

    @Column(nullable = false, name = "summary")
    private String summary;

    @Column(length = 5000, nullable = false, name = "content")
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "published")
    private LocalDateTime published;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private ProofStatus status;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "talent_id", referencedColumnName = "id")
    private Talent talent;

    @Column(nullable = false, name = "kudos")
    private int kudos;

    @OneToMany(mappedBy = "proof")
    private Set<KudosHistory> kudosHistory;

    @Transient
    private boolean kudosedByMe;

    @OneToMany(mappedBy = "proof")
    private Set<SkillKudos> skillKudos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proof proof = (Proof) o;
        return kudos == proof.kudos && Objects.equals(id, proof.id) && Objects.equals(iconNumber, proof.iconNumber) && Objects.equals(title, proof.title) && Objects.equals(summary, proof.summary) && Objects.equals(content, proof.content) && Objects.equals(published, proof.published) && status == proof.status;
    }

    @Override
    public String toString() {
        return "Proof{" +
                "id=" + id +
                ", iconNumber=" + iconNumber +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", content='" + content + '\'' +
                ", published=" + published +
                ", status=" + status +
                ", kudos=" + kudos +
                ", skillKudos=" + skillKudos +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, iconNumber, title, summary, content, published, status, kudos);
    }
}
