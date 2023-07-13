package com.uptalent.answer.model.entity;

import com.uptalent.answer.model.enums.MessageStatus;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.vacancy.submission.model.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "answer")
@Table(name = "answer")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    @Column(nullable = false, name = "message", length = 1000)
    private String message;

    @Column(name = "title")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private MessageStatus status;

    @Column(nullable = false, name = "is_templated_message")
    private Boolean isTemplatedMessage;

    @ManyToOne
    @JoinColumn(name = "sponsor_id", referencedColumnName = "id")
    private Sponsor sponsor;
}