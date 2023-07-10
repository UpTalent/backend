package com.uptalent.vacancy.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.model.SkillVacancyInfo;
import com.uptalent.util.model.response.Author;
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
    private Author author;
    private boolean canSubmit;
}
