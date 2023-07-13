package com.uptalent.answer.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedbackResponse {
    private String contactInfo;
    private String message;
}
