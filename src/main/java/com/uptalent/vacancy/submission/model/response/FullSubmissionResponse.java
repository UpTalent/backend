package com.uptalent.vacancy.submission.model.response;

import com.uptalent.util.model.response.Author;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullSubmissionResponse extends SubmissionResponse {
    private String contactInfo;
    private String message;
    private Author author;
}
