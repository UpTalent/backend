package com.uptalent.credentials.repository;

import com.uptalent.credentials.model.entity.Credentials;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.talent.model.entity.Talent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    Optional<Credentials> findByEmailIgnoreCase(String email);

    @Query(value = "SELECT cr " +
            "FROM credentials cr " +
            "WHERE LOWER(cr.email) = LOWER(:email) " +
            "AND cr.status = com.uptalent.credentials.model.enums.AccountStatus.ACTIVE")
    Optional<Credentials> findActiveEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Query(value = "UPDATE credentials cr " +
            "SET cr.expirationDeleting=NULL, " +
            "cr.deleteToken=NULL, " +
            "cr.email='Deleted', cr.status=com.uptalent.credentials.model.enums.AccountStatus.PERMANENTLY_DELETED " +
            "WHERE cr.id IN :collect")
    void updateCredentialsDeleteData(List<Long> collect);
}
