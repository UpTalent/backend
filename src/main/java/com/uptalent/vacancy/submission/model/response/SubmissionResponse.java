package com.uptalent.vacancy.submission.model.response;

import com.uptalent.vacancy.submission.model.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponse {
    private long id;
    private LocalDateTime sent;
    private SubmissionStatus status;
}
