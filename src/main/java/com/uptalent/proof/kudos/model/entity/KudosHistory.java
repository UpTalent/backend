package com.uptalent.proof.kudos.model.entity;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity(name = "kudos_history")
@Table(name = "kudos_history")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KudosHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "talent_id", referencedColumnName = "id")
    private Talent talent;

    @ManyToOne
    @JoinColumn(name = "proof_id", referencedColumnName = "id")
    private Proof proof;

    @Column(nullable = false, name = "sent")
    private LocalDateTime sent;

    @ColumnDefault("0")
    @Column(nullable = false, name = "kudos")
    private int kudos;
}
