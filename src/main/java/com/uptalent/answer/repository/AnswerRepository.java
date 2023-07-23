package com.uptalent.answer.repository;

import com.uptalent.answer.model.entity.Answer;
import com.uptalent.sponsor.model.entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("SELECT a FROM answer a WHERE a.sponsor = :sponsor AND a.isTemplatedMessage = true")
    List<Answer> findAllBySponsor(Sponsor sponsor);
}
