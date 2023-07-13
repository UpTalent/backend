package com.uptalent.vacancy.submission.model.response;

import com.uptalent.answer.model.request.FeedbackResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TalentSubmission {
    private VacancySubmission vacancySubmission;
    private SubmissionResponse submissionResponse;
    private FeedbackResponse feedbackResponse;
}
