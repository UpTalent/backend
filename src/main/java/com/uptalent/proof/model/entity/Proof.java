package com.uptalent.proof.model.entity;

import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

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

}
