package com.uptalent.vacancy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.model.SkillVacancyInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class VacancyDetailInfo {
    private Long id;
    private String title;
    private String content;
    private ContentStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime published;
    private Set<SkillVacancyInfo> skills;
}
