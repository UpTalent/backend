package com.uptalent.vacancy.model.response;

import com.uptalent.skill.model.SkillVacancyInfo;
import com.uptalent.util.model.response.Author;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class VacancyGeneralInfo {
    private Long id;
    private String title;
    private LocalDateTime published;
    private Set<SkillVacancyInfo> skills;
    private Author author;
}
