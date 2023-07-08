package com.uptalent.mapper;

import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.model.SkillVacancyInfo;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.util.model.response.Author;
import com.uptalent.vacancy.model.entity.Vacancy;
import com.uptalent.vacancy.model.response.VacancyDetailInfo;
import com.uptalent.vacancy.model.request.VacancyModify;
import com.uptalent.vacancy.model.response.VacancyGeneralInfo;
import com.uptalent.vacancy.submission.model.entity.Submission;
import com.uptalent.vacancy.submission.model.request.SubmissionRequest;
import com.uptalent.vacancy.submission.model.response.SubmissionResponse;
import com.uptalent.vacancy.submission.model.response.VacancySubmission;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VacancyMapper {
    default Vacancy toVacancy(VacancyModify vacancyModify) {
        return Vacancy.builder()
                .title(vacancyModify.getTitle())
                .content(vacancyModify.getContent())
                .status(ContentStatus.valueOf(vacancyModify.getStatus()))
                .skillsMatchedPercent(vacancyModify.getSkillsMatchedPercent() == null ? 100 : vacancyModify.getSkillsMatchedPercent())
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
                        .map(skill -> new SkillVacancyInfo(skill.getId(), skill.getName()))
                        .collect(Collectors.toSet()))
                .author(toAuthor(vacancy.getSponsor()))
                .build();
    }
    default List<VacancyGeneralInfo> toVacancyGeneralInfos(List<Vacancy> vacancies) {
        return vacancies.stream().map(this::toVacancyGeneralInfo).toList();
    }

    default VacancyGeneralInfo toVacancyGeneralInfo(Vacancy vacancy) {
        return VacancyGeneralInfo.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .published(vacancy.getPublished())
                .skills(vacancy.getSkills().stream()
                        .map(skill -> new SkillVacancyInfo(skill.getId(), skill.getName()))
                        .collect(Collectors.toSet()))
                .author(toAuthor(vacancy.getSponsor()))
                .build();

    }

    Submission toSubmission(SubmissionRequest submissionRequest);

    SubmissionResponse toSubmissionResponse(Submission submission);

    default VacancySubmission toVacancySubmission(Vacancy vacancy){
        return VacancySubmission.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .author(toAuthor(vacancy.getSponsor()))
                .build();
    }

    default Author toAuthor(Sponsor sponsor){
        return Author.builder()
                .id(sponsor.getId())
                .name(sponsor.getFullname())
                .avatar(sponsor.getAvatar())
                .build();
    }
}
