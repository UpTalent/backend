package com.uptalent.sponsor.repository;

import com.uptalent.sponsor.model.entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
}
