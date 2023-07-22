package com.uptalent.vacancy.submission.model.response;

import com.uptalent.vacancy.submission.model.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubmissionResponse {
    private LocalDateTime sent;
    private SubmissionStatus status;
}
