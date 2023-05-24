package com.uptalent.credentials.model.entity;

import com.uptalent.credentials.model.enums.AccountStatus;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.talent.model.entity.Talent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "credentials")
@Table(name = "credentials")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(length = 100, nullable = false, name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @OneToOne(mappedBy = "credentials")
    private Sponsor sponsor;

    @OneToOne(mappedBy = "credentials")
    private Talent talent;

    @Column(name = "delete_token")
    private String deleteToken;

    @Column(name = "expiration_deleting")
    private LocalDateTime expirationDeleting;

    @Column(name = "verified")
    private Boolean verified;
}
