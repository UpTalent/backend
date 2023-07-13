package com.uptalent.answer.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedbackContent {
    private Long feedbackId;
    private Feedback feedback;
}
