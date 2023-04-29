package com.uptalent.credentials.repository;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.talent.model.entity.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    Optional<Credentials> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
