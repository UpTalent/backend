package com.uptalent.vacancy.submission.model.response;

import com.uptalent.answer.model.response.FeedbackInfo;
import com.uptalent.util.model.response.Author;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FullSubmissionResponse extends SubmissionResponse {
    private String contactInfo;
    private String message;
    private Author author;
    private FeedbackInfo feedbackInfo;
}
