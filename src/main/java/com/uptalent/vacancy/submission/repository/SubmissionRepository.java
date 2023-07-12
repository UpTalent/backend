package com.uptalent.vacancy.submission.repository;

import com.uptalent.vacancy.submission.model.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Page<Submission> findSubmissionsByTalentId(Pageable pageable, Long talentId);
    Optional<Submission> findSubmissionByTalentIdAndVacancyId(Long talentId, Long vacancyId);
}
