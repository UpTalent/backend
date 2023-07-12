package com.uptalent.mapper;

import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.model.SkillVacancyInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.util.model.response.Author;
import com.uptalent.vacancy.model.entity.Vacancy;
import com.uptalent.vacancy.model.response.SponsorVacancyDetailInfo;
import com.uptalent.vacancy.model.response.TalentVacancyDetailInfo;
import com.uptalent.vacancy.model.response.VacancyDetailInfo;
import com.uptalent.vacancy.model.request.VacancyModify;
import com.uptalent.vacancy.model.response.VacancyGeneralInfo;
import com.uptalent.vacancy.submission.model.entity.Submission;
import com.uptalent.vacancy.submission.model.request.SubmissionRequest;
import com.uptalent.vacancy.submission.model.response.SubmissionResponse;
import com.uptalent.vacancy.submission.model.response.VacancySubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface VacancyMapper {
    default Vacancy toVacancy(VacancyModify vacancyModify) {
        return Vacancy.builder()
                .title(vacancyModify.getTitle())
                .content(vacancyModify.getContent())
                .status(ContentStatus.valueOf(vacancyModify.getStatus()))
                .skillsMatchedPercent(vacancyModify.getSkillsMatchedPercent() == null ? 100 : vacancyModify.getSkillsMatchedPercent())
                .build();
    }

    @Mapping(source = "vacancy.skills", target = "skills")
    @Mapping(source = "vacancy.sponsor", target = "author")
    VacancyDetailInfo toVacancyDetailInfo(Vacancy vacancy);

    @Mapping(source = "vacancy.skills", target = "skills")
    @Mapping(source = "vacancy.sponsor", target = "author")
    TalentVacancyDetailInfo toTalentVacancyDetailInfo(Vacancy vacancy);

    @Mapping(source = "vacancy.skills", target = "skills")
    @Mapping(source = "vacancy.sponsor", target = "author")
    @Mapping(source = "vacancy.submissions", target = "submissions")
    SponsorVacancyDetailInfo toSponsorVacancyDetailInfo(Vacancy vacancy);

    List<VacancyGeneralInfo> toVacancyGeneralInfos(List<Vacancy> vacancies);

    @Mapping(source = "vacancy.skills", target = "skills")
    @Mapping(source = "vacancy.sponsor", target = "author")
    VacancyGeneralInfo toVacancyGeneralInfo(Vacancy vacancy);

    Submission toSubmission(SubmissionRequest submissionRequest);

    SubmissionResponse toSubmissionResponse(Submission submission);

    @Mapping(source = "vacancy.sponsor", target = "author")
    VacancySubmission toVacancySubmission(Vacancy vacancy);

    @Mapping(source = "sponsor.id", target = "id")
    @Mapping(source = "sponsor.fullname", target = "name")
    @Mapping(source = "sponsor.avatar", target = "avatar")
    Author toAuthor(Sponsor sponsor);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    SkillVacancyInfo toSkillVacancyInfo(Skill skill);
}
