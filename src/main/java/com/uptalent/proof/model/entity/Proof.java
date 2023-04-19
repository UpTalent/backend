package com.uptalent.proof.model.entity;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proof {
    @Id
    @GeneratedValue
    private Long id;

    private Integer iconNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String summary;

    @Column(length = 5000, nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime published;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProofStatus status;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "talent_id", referencedColumnName = "id")
    private Talent talent;

    @Column(nullable = false)
    private int kudos;

    @OneToMany(mappedBy = "proof")
    private Set<KudosHistory> kudosHistory;
}
