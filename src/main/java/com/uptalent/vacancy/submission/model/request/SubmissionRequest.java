package com.uptalent.vacancy.submission.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmissionRequest {
    @NotBlank(message = "Contact info should not be blank")
    @Size(max = 100, message = "Contact info must be less than 100 characters")
    private String contactInfo;

    @NotBlank(message = "Message should not be blank")
    @Size(max = 1000, message = "Message must be less than 1000 characters")
    private String message;
}
