package com.uptalent.vacancy.submission.repository;

import com.uptalent.vacancy.submission.model.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}
