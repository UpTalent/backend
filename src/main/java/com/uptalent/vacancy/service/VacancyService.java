package com.uptalent.vacancy.service;

import com.uptalent.mapper.VacancyMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.proof.exception.WrongSortOrderException;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.exception.SkillNotFoundException;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.repository.SkillRepository;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.util.exception.IllegalContentModifyingException;
import com.uptalent.util.exception.UnrelatedContentException;
import com.uptalent.util.service.AccessVerifyService;
import com.uptalent.vacancy.exception.NoSuchMatchedSkillsException;
import com.uptalent.vacancy.exception.VacancyNotFoundException;
import com.uptalent.vacancy.model.entity.Vacancy;
import com.uptalent.vacancy.model.response.VacancyGeneralInfo;
import com.uptalent.vacancy.repository.VacancyRepository;
import com.uptalent.vacancy.model.response.VacancyDetailInfo;
import com.uptalent.vacancy.model.request.VacancyModify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.uptalent.credentials.model.enums.Role.SPONSOR;
import static com.uptalent.proof.model.enums.ContentStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VacancyService {
    private final VacancyRepository vacancyRepository;
    private final SponsorRepository sponsorRepository;
    private final SkillRepository skillRepository;
    private final VacancyMapper vacancyMapper;
    private final AccessVerifyService accessVerifyService;
    private final TalentRepository talentRepository;

    public URI createVacancy(VacancyModify vacancyModify) {
        if (vacancyModify.getStatus().equals(ContentStatus.HIDDEN.name()))
            throw new VacancyNotFoundException("Vacancy cannot be HIDDEN");

        Vacancy vacancy = vacancyMapper.toVacancy(vacancyModify);
        setSkills(vacancyModify, vacancy);
        Sponsor sponsor = sponsorRepository.findById(accessVerifyService.getPrincipalId())
                .orElseThrow(() -> new SponsorNotFoundException("Sponsor was not found"));
        vacancy.setSponsor(sponsor);

        if (vacancyModify.getStatus().equals(ContentStatus.PUBLISHED.name())) {
            if (vacancyModify.getSkillIds().isEmpty())
                throw new IllegalContentModifyingException("Skills should be added for publishing a vacancy");
            vacancy.setPublished(LocalDateTime.now());
        }

        vacancy = vacancyRepository.save(vacancy);

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(vacancy.getId())
                .toUri();
    }

    @Transactional(readOnly = true)
    public VacancyDetailInfo getVacancy(Long id) {
        Vacancy vacancy = getVacancyById(id);

        if(!vacancy.getStatus().equals(PUBLISHED))
            accessVerifyService.tryGetAccess(vacancy.getSponsor().getId(), SPONSOR,
                    "You do not have permission to get vacancy");

        return vacancyMapper.toVacancyDetailInfo(vacancy);
    }

    public VacancyDetailInfo updateVacancy(Long id, VacancyModify vacancyModify) {
        Vacancy vacancy = getVacancyById(id);
        verifySponsorContainVacancy(accessVerifyService.getPrincipalId(), vacancy);

        Consumer<Vacancy> modifyingStrategy = selectVacancyModifyStrategy(vacancyModify, vacancy.getStatus());

        modifyingStrategy.accept(vacancy);
        return vacancyMapper.toVacancyDetailInfo(vacancy);
    }

    private Consumer<Vacancy> selectVacancyModifyStrategy(VacancyModify vacancyModify, ContentStatus currentStatus) {
        Consumer<Vacancy> strategy;
        ContentStatus modifyingStatus = ContentStatus.valueOf(vacancyModify.getStatus());
        BiPredicate<VacancyModify, ContentStatus> editCase = (vacancyEdit, status) ->
                modifyingStatus.equals(DRAFT) && status.equals(DRAFT);
        BiPredicate<VacancyModify, ContentStatus> publishCase = (vacancyEdit, status) ->
                modifyingStatus.equals(PUBLISHED) && status.equals(DRAFT);
        BiPredicate<VacancyModify, ContentStatus> hideCase = (vacancyEdit, status) ->
                modifyingStatus.equals(HIDDEN) && status.equals(PUBLISHED);
        BiPredicate<VacancyModify, ContentStatus> reopenCase = (vacancyEdit, status) ->
                modifyingStatus.equals(PUBLISHED) && status.equals(HIDDEN);

        if (editCase.test(vacancyModify, currentStatus))
            strategy = vacancy -> updateVacancyData(vacancyModify, vacancy);
        else if (publishCase.test(vacancyModify, currentStatus))
            strategy = vacancy -> publishVacancy(vacancyModify, vacancy);
        else if (hideCase.test(vacancyModify, currentStatus))
            strategy = vacancy -> vacancy.setStatus(HIDDEN);
        else if (reopenCase.test(vacancyModify, currentStatus))
            strategy = vacancy -> vacancy.setStatus(PUBLISHED);
        else
            throw new IllegalContentModifyingException("Illegal operation for modifying status ["
                    + currentStatus + " -> " + vacancyModify.getStatus() + "]");

        return strategy;
    }

    public PageWithMetadata<VacancyDetailInfo> getSponsorVacancies(int page, int size, String sort,
                                                                       Long sponsorId, String status) {

        ContentStatus contentStatus = ContentStatus.valueOf(status.toUpperCase());
        Sort sortOrder = getSortByString(sort, contentStatus);
        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);

        if (!PUBLISHED.equals(contentStatus))
            accessVerifyService.tryGetAccess(sponsorId, SPONSOR,
                    "You do not have permission to get list of vacancies");

        Page<Vacancy> vacanciesPage = vacancyRepository.findVacanciesBySponsorId(sponsorId, contentStatus, pageRequest);
        List<VacancyDetailInfo> vacancyDetailInfos = vacanciesPage.getContent().stream()
                .map(vacancyMapper::toVacancyDetailInfo).toList();

        return new PageWithMetadata<>(vacancyDetailInfos, vacanciesPage.getTotalPages());
    }

    private void publishVacancy(VacancyModify vacancyModify, Vacancy vacancy) {
        if (vacancyModify.getSkillIds().isEmpty()) {
            throw new IllegalContentModifyingException("Skills should be set for publishing");
        }
        updateVacancyData(vacancyModify, vacancy);
        vacancy.setPublished(LocalDateTime.now());
        vacancy.setStatus(PUBLISHED);
    }
    public PageWithMetadata<VacancyGeneralInfo> getVacancies(int page, int size, String sort, String [] skills) {
        Sort sortOrder = getSortByString(sort, PUBLISHED);
        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);
        int skillsSize = (skills == null) ? 0 : skills.length;
        Page<Vacancy> vacanciesPage = vacancyRepository.findVacancies(PUBLISHED, pageRequest, skills, skillsSize);
        List<Vacancy> retrievedVacancies = vacanciesPage.getContent();
        List<VacancyGeneralInfo> proofGeneralInfos = vacancyMapper.toVacancyGeneralInfos(retrievedVacancies);
        return new PageWithMetadata<>(proofGeneralInfos, vacanciesPage.getTotalPages());
    }
    @PreAuthorize("hasAuthority('SPONSOR')")
    @Transactional
    public void deleteVacancy(Long vacancyId) {
        Vacancy vacancyToDelete = getVacancyById(vacancyId);
        verifySponsorContainVacancy(accessVerifyService.getPrincipalId(), vacancyToDelete);
        vacancyRepository.delete(vacancyToDelete);
    }

    private void updateVacancyData(VacancyModify vacancyModify, Vacancy vacancy) {
        vacancy.setTitle(vacancyModify.getTitle());
        vacancy.setContent(vacancyModify.getContent());
        clearSkills(vacancy);
        setSkills(vacancyModify, vacancy);
    }

    private void clearSkills(Vacancy vacancy) {
        if (vacancy.getSkills() != null && !vacancy.getSkills().isEmpty())
            vacancy.getSkills().clear();
        vacancyRepository.save(vacancy);
    }

    private void setSkills(VacancyModify vacancyModify, Vacancy vacancy) {
        Set<Long> skillsIds = new HashSet<>(vacancyModify.getSkillIds());
        int uniqueSkillIds = skillsIds.size();
        if (uniqueSkillIds != vacancyModify.getSkillIds().size())
            throw new SkillNotFoundException("Some skills have duplicates");

        Set<Skill> skills = new HashSet<>(skillRepository.findAllById(skillsIds));
        if(uniqueSkillIds != skills.size())
            throw new SkillNotFoundException("Some skills which are not exist");

        vacancy.setSkills(skills);
    }

    private Vacancy getVacancyById(Long id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy was not found"));
    }

    private void verifySponsorContainVacancy(Long principalId, Vacancy vacancy) {
        if (!Objects.equals(principalId, vacancy.getSponsor().getId()))
            throw new UnrelatedContentException("You cannot update vacancy for other sponsor");
    }

    private Sort getSortByString(String sort, ContentStatus status){
        String sortField = status.equals(DRAFT) ? "id" : "published";

        if(sort.equals("desc"))
            return Sort.by(sortField).descending();
        else if (sort.equals("asc"))
            return Sort.by(sortField).ascending();
        else
            throw new WrongSortOrderException("Unexpected input of sort order");
    }

    private void verifyMatchedSkills(Vacancy vacancy) {
        Set<Skill> talentSkills = talentRepository
                .findById(accessVerifyService.getPrincipalId())
                .get().getSkills();

        int requiredSkillsNumber = (int) (((double) vacancy.getSkillsMatchedPercent()/100) * vacancy.getSkills().size());

        if (talentSkills.size() < requiredSkillsNumber)
            throw new NoSuchMatchedSkillsException("You do not have enough skills to watch this vacancy");
    }
}
