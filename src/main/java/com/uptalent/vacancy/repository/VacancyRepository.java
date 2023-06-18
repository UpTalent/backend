package com.uptalent.vacancy.repository;

import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.vacancy.model.entity.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    @Query("SELECT v " +
            "FROM vacancy v " +
            "WHERE v.status = :contentStatus AND v.sponsor.id = :sponsorId")
    Page<Vacancy> findVacanciesBySponsorId(Long sponsorId,
                                                    ContentStatus contentStatus,
                                                    Pageable pageable);
}
