package com.uptalent.mapper;

import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.model.SkillVacancyInfo;
import com.uptalent.vacancy.model.entity.Vacancy;
import com.uptalent.vacancy.model.response.VacancyDetailInfo;
import com.uptalent.vacancy.model.request.VacancyModify;
import org.mapstruct.Mapper;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VacancyMapper {
    default Vacancy toVacancy(VacancyModify vacancyModify) {
        return Vacancy.builder()
                .title(vacancyModify.getTitle())
                .content(vacancyModify.getContent())
                .status(ContentStatus.valueOf(vacancyModify.getStatus()))
                .build();
    }

    default VacancyDetailInfo toVacancyDetailInfo(Vacancy vacancy) {
        return VacancyDetailInfo.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .content(vacancy.getContent())
                .published(vacancy.getPublished())
                .status(vacancy.getStatus())
                .skills(vacancy.getSkills().stream()
                        .map(skill -> new SkillVacancyInfo(skill.getName()))
                        .collect(Collectors.toSet()))
                .build();
    }
}
