package com.uptalent.vacancy.submission.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TalentSubmission {
    private VacancySubmission vacancySubmission;
    private SubmissionResponse submissionResponse;
}
