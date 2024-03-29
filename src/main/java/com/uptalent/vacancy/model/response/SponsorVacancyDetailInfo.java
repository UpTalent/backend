package com.uptalent.vacancy.model.response;

import com.uptalent.vacancy.submission.model.response.FullSubmissionResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SponsorVacancyDetailInfo extends VacancyDetailInfo{
    private List<FullSubmissionResponse> submissions;
}
