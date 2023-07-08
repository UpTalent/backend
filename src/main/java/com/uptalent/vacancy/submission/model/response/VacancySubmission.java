package com.uptalent.vacancy.submission.model.response;

import com.uptalent.util.model.response.Author;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class VacancySubmission {
    private Long id;
    private String title;
    private Author author;
}
