package com.uptalent.vacancy.model.response;

import com.uptalent.vacancy.submission.model.response.SubmissionResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TalentVacancyDetailInfo extends VacancyDetailInfo {
    private SubmissionResponse mySubmission;
    private boolean canSubmit;
}
