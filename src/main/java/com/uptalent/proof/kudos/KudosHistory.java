package com.uptalent.proof.kudos;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KudosHistory {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "talent_id", referencedColumnName = "id")
    private Talent talent;

    @ManyToOne
    @JoinColumn(name = "proof_id", referencedColumnName = "id")
    private Proof proof;

    private LocalDateTime sent;

    private int kudos;
}
