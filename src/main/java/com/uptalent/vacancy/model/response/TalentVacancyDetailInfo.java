package com.uptalent.vacancy.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TalentVacancyDetailInfo extends VacancyDetailInfo {
    private boolean canSubmit;
}
