package com.uptalent.sponsor.model.entity;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.proof.kudos.model.entity.KudosHistory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity(name = "sponsor")
@Table(name = "sponsor")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sponsor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinTable(
            name = "sponsor_credentials",
            joinColumns = @JoinColumn(name = "sponsor_id"),
            inverseJoinColumns = @JoinColumn(name = "credentials_id")
    )
    private Credentials credentials;

    @Column(length = 30, nullable = false, name = "fullname")
    private String fullname;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "kudos")
    private long kudos;

    @OneToMany(mappedBy = "sponsor")
    private Set<KudosHistory> kudosHistory;
}
