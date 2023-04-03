package com.uptalent.proof.model.entity;

import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private LocalDateTime published;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProofStatus status;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "talent_id", referencedColumnName = "id")
    private Talent talent;
}