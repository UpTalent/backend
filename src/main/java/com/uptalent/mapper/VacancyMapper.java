package com.uptalent.mapper;

import com.uptalent.answer.model.entity.Answer;
import com.uptalent.answer.model.request.FeedbackResponse;
import com.uptalent.answer.model.response.FeedbackInfo;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.model.SkillVacancyInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.util.model.response.Author;
import com.uptalent.vacancy.model.entity.Vacancy;
import com.uptalent.vacancy.model.response.SponsorVacancyDetailInfo;
import com.uptalent.vacancy.model.response.TalentVacancyDetailInfo;
import com.uptalent.vacancy.model.response.VacancyDetailInfo;
import com.uptalent.vacancy.model.request.VacancyModify;
import com.uptalent.vacancy.model.response.VacancyGeneralInfo;
import com.uptalent.vacancy.submission.model.entity.Submission;
import com.uptalent.vacancy.submission.model.request.SubmissionRequest;
import com.uptalent.vacancy.submission.model.response.FullSubmissionResponse;
import com.uptalent.vacancy.submission.model.response.SubmissionResponse;
import com.uptalent.vacancy.submission.model.response.VacancySubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

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
    default List<FullSubmissionResponse> toFullSubmissionResponses(Vacancy vacancy, FeedbackMapper feedbackMapper){
        return vacancy
                .getSubmissions()
                .stream()
                .map(s ->
                        FullSubmissionResponse.builder()
                                .feedbackInfo(feedbackMapper.toFeedbackInfo(s.getAnswer()))
                                .author(toAuthor(s.getTalent()))
                                .message(s.getMessage())
                                .contactInfo(s.getContactInfo())
                                .sent(s.getSent())
                                .status(s.getStatus())
                                .id(s.getId())
                                .build()
                ).collect(Collectors.toList());
    }
    default SponsorVacancyDetailInfo toSponsorVacancyDetailInfo(Vacancy vacancy, List<FullSubmissionResponse> fullSubmissionResponses){
        return SponsorVacancyDetailInfo.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .content(vacancy.getContent())
                .status(vacancy.getStatus())
                .published(vacancy.getPublished())
                .skills(vacancy.getSkills().stream().map(this::toSkillVacancyInfo).collect(Collectors.toSet()))
                .author(toAuthor(vacancy.getSponsor()))
                .submissions(fullSubmissionResponses)
                .build();
    }
    default FullSubmissionResponse toFullSubmissionResponse(Submission submission, FeedbackInfo feedbackInfo){
        return FullSubmissionResponse.builder()
                .feedbackInfo(feedbackInfo)
                .author(toAuthor(submission.getTalent()))
                .message(submission.getMessage())
                .contactInfo(submission.getContactInfo())
                .sent(submission.getSent())
                .status(submission.getStatus())
                .id(submission.getId())
                .build();
    }
    @Mapping(source = "vacancy.skills", target = "skills")
    @Mapping(source = "vacancy.sponsor", target = "author")
    VacancyDetailInfo toVacancyDetailInfo(Vacancy vacancy);

    @Mapping(source = "vacancy.skills", target = "skills")
    @Mapping(source = "vacancy.sponsor", target = "author")
    TalentVacancyDetailInfo toTalentVacancyDetailInfo(Vacancy vacancy);

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

    @Mapping(source = "talent.id", target = "id")
    @Mapping(source = "talent.firstname", target = "name")
    @Mapping(source = "talent.avatar", target = "avatar")
    Author toAuthor(Talent talent);

    @Mapping(source = "submission.talent", target = "author")
    FullSubmissionResponse toFullSubmissionResponse(Submission submission);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    SkillVacancyInfo toSkillVacancyInfo(Skill skill);

    @Mapping(source = "contactInfo", target = "contactInfo")
    @Mapping(source = "message", target = "message")
    FeedbackResponse toFeedbackResponse(Answer answer);
}
