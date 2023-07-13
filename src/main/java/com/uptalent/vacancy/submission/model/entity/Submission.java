package com.uptalent.vacancy.submission.model.entity;

import com.uptalent.answer.model.entity.Answer;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.vacancy.model.entity.Vacancy;
import com.uptalent.vacancy.submission.model.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "submission")
@Table(name = "submission")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "contact_info", length = 100)
    private String contactInfo;

    @Column(nullable = false, name = "message", length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private SubmissionStatus status;

    @Column(nullable = false, name = "sent")
    private LocalDateTime sent;

    @ManyToOne
    @JoinColumn(name = "vacancy_id", referencedColumnName = "id")
    private Vacancy vacancy;

    @ManyToOne
    @JoinColumn(name = "talent_id", referencedColumnName = "id")
    private Talent talent;

    @OneToOne
    @JoinColumn(name = "answer_id", referencedColumnName = "id")
    private Answer answer;
}
