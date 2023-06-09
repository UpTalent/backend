package com.uptalent.vacancy;

import com.uptalent.mapper.VacancyMapper;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.skill.exception.SkillNotFoundException;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.repository.SkillRepository;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.util.service.AccessVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class VacancyService {
    private final VacancyRepository vacancyRepository;
    private final SponsorRepository sponsorRepository;
    private final SkillRepository skillRepository;
    private final VacancyMapper vacancyMapper;
    private final AccessVerifyService accessVerifyService;

    @PreAuthorize("hasAuthority('SPONSOR')")
    public URI createVacancy(VacancyModify vacancyModify) {
        if (vacancyModify.getStatus().equals(ContentStatus.HIDDEN.name()))
            throw new VacancyNotFoundException("Vacancy cannot be HIDDEN");

        Vacancy vacancy = vacancyMapper.toVacancy(vacancyModify);
        setSkills(vacancyModify, vacancy);
        Sponsor sponsor = sponsorRepository.findById(accessVerifyService.getPrincipalId())
                .orElseThrow(() -> new SponsorNotFoundException("Sponsor was not found"));
        vacancy.setSponsor(sponsor);

        if (vacancyModify.getStatus().equals(ContentStatus.PUBLISHED.name())) {
            if (vacancyModify.getSkillIds() != null && vacancyModify.getSkillIds().isEmpty())
                throw new IllegalVacancyModifyingException("Skills should be added for publishing a vacancy");
            vacancy.setPublished(LocalDateTime.now());
        }

        vacancy = vacancyRepository.save(vacancy);

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(vacancy.getId())
                .toUri();
    }

    public VacancyDetailInfo getVacancyById(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException("Vacancy was not found"));

        return vacancyMapper.toVacancyDetailInfo(vacancy);
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
}
