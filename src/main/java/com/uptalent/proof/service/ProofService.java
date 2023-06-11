package com.uptalent.proof.service;

import com.uptalent.credentials.model.enums.Role;
import com.uptalent.mapper.KudosHistoryMapper;
import com.uptalent.mapper.ProofMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.proof.exception.*;
import com.uptalent.proof.kudos.exception.IllegalPostingKudos;
import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.request.PostKudos;
import com.uptalent.proof.kudos.model.request.PostKudosSkill;
import com.uptalent.proof.kudos.model.response.KudosSender;
import com.uptalent.proof.kudos.model.response.UpdatedProofKudos;
import com.uptalent.proof.kudos.repository.KudosHistoryRepository;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofGeneralInfo;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.skill.exception.DuplicateSkillException;
import com.uptalent.skill.exception.SkillNotFoundException;
import com.uptalent.skill.model.SkillProofInfo;
import com.uptalent.skill.model.entity.Skill;
import com.uptalent.skill.model.entity.SkillKudos;
import com.uptalent.skill.model.entity.SkillKudosHistory;
import com.uptalent.skill.repository.SkillKudosHistoryRepository;
import com.uptalent.skill.repository.SkillKudosRepository;
import com.uptalent.skill.repository.SkillRepository;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.util.exception.IllegalContentModifyingException;
import com.uptalent.util.exception.UnrelatedContentException;
import com.uptalent.util.service.AccessVerifyService;
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
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.uptalent.credentials.model.enums.Role.SPONSOR;
import static com.uptalent.credentials.model.enums.Role.TALENT;
import static com.uptalent.proof.model.enums.ContentStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProofService {
    private final ProofRepository proofRepository;
    private final TalentRepository talentRepository;
    private final KudosHistoryRepository kudosHistoryRepository;
    private final ProofMapper mapper;
    private final AccessVerifyService accessVerifyService;
    private final SponsorRepository sponsorRepository;
    private final SkillRepository skillRepository;
    private final SkillKudosRepository skillKudosRepository;
    private final SkillKudosHistoryRepository skillKudosHistoryRepository;
    private final KudosHistoryMapper kudosHistoryMapper;

    private long KUDOS_MAX_VALUE = 9999999999L;


    public PageWithMetadata<? extends ProofGeneralInfo> getProofs(int page, int size, String sort, String [] skills) {
        Sort sortOrder = getSortByString(sort, PUBLISHED);
        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);
        Long principalId = accessVerifyService.getPrincipalId();
        Page<? extends ProofGeneralInfo> proofsPage = getPageProofsWithGeneralInfo(principalId, pageRequest, skills);

        return new PageWithMetadata<>(proofsPage.getContent(), proofsPage.getTotalPages());
    }

    public ProofDetailInfo getProofDetailInfo(Long talentId, Long proofId) {
        verifyTalentExistsById(talentId);
        accessVerifyService.tryGetAccess(
                talentId,
                Role.TALENT,
                "You cannot get proof detail info"
        );
        Proof proof = getProofById(proofId);

        verifyTalentContainProof(talentId, proof);

        return mapper.toProofDetailInfo(proof);
    }

    @PreAuthorize("hasAuthority('TALENT')")
    @Transactional
    public URI createProof(ProofModify proofModify, Long talentId) {
        Talent talent = getTalentById(talentId);

        accessVerifyService.tryGetAccess(
                talentId,
                Role.TALENT,
                "You do not have permission to create proof"
        );

        Proof proof = mapper.toProof(proofModify);
        proof.setTalent(talent);
        proof = proofRepository.save(proof);

        setSkills(proofModify, proof);

        talent.getProofs().add(proof);
        talentRepository.save(talent);

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(proof.getId())
                .toUri();
    }

    @PreAuthorize("hasAuthority('TALENT')")
    @Transactional
    public ProofDetailInfo editProof(ProofModify proofModify, Long talentId, Long proofId) {
        verifyTalentExistsById(talentId);
        accessVerifyService.tryGetAccess(
                talentId,
                Role.TALENT,
                "You do not have permission to edit proof"
        );
        Proof foundProof = getProofById(proofId);
        verifyTalentContainProof(talentId, foundProof);

        Consumer<Proof> modifyingStrategy = selectProofModifyStrategy(proofModify,
                foundProof.getId(),
                foundProof.getStatus());

        modifyingStrategy.accept(foundProof);

        return mapper.toProofDetailInfo(foundProof);
    }


    public PageWithMetadata<? extends ProofDetailInfo> getTalentProofs(int page, int size, String sort,
                                                             Long talentId, String status) {

        ContentStatus contentStatus = ContentStatus.valueOf(status.toUpperCase());
        Sort sortOrder = getSortByString(sort, contentStatus);
        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);
        Long principalId = accessVerifyService.getPrincipalId();

        validateGetTalentProofs(talentId, contentStatus);

        Page<? extends ProofDetailInfo> proofsPage = getPageProofsWithDetailInfo(principalId, talentId,
                contentStatus, pageRequest);

        return new PageWithMetadata<>(proofsPage.getContent(), proofsPage.getTotalPages());
    }

    @PreAuthorize("hasAuthority('TALENT')")
    @Transactional
    public void deleteProof(Long proofId, Long talentId) {
        verifyTalentExistsById(talentId);
        accessVerifyService.tryGetAccess(
                talentId,
                Role.TALENT,
                "You do not have permission to delete proof"
        );

        Proof proofToDelete = getProofById(proofId);
        verifyTalentContainProof(talentId, proofToDelete);

        proofRepository.delete(proofToDelete);
    }

    public List<KudosSender> getKudosSenders(Long proofId) {
        verifyProofExistsById(proofId);
        verifyTalentContainProof(accessVerifyService.getPrincipalId(), getProofById(proofId));
        return kudosHistoryRepository.findKudosSendersByProofId(proofId).stream()
                .map(kudosHistoryMapper::toKudosedSender)
                .collect(Collectors.toList());
    }


    @PreAuthorize("hasAuthority('SPONSOR')")
    @Transactional
    public UpdatedProofKudos postKudos(PostKudos postKudos, Long proofId) {
        Long sponsorId = accessVerifyService.getPrincipalId();
        Proof proof = getProofById(proofId);
        Sponsor sponsor = getSponsorById(sponsorId);

        long sumKudos = postKudos.getPostKudosSkills().stream()
                .mapToLong(PostKudosSkill::getKudos)
                .sum();
        validatePostingKudos(sponsor, sumKudos, proof);

        Set<Long> skillIdsSet = postKudos.getPostKudosSkills().stream()
                .map(PostKudosSkill::getSkillId)
                .collect(Collectors.toSet());
        List<Long> skillsIdList = postKudos.getPostKudosSkills().stream()
                .map(PostKudosSkill::getSkillId)
                .toList();

        int uniqueSkillIds = skillIdsSet.size();
        validateNotContainsDuplicates(uniqueSkillIds, postKudos.getPostKudosSkills().size());

        Set<Skill> skills = new HashSet<>(skillRepository.findAllById(skillsIdList));
        if(uniqueSkillIds != skills.size()) {
            throw new SkillNotFoundException("Some skills which are not exist");
        }
        validateProofContainSkills(proof, skills);
        validatePositiveKudoses(postKudos);

        KudosHistory kudosHistory = KudosHistory.builder()
                .sponsor(sponsor)
                .proof(proof)
                .sent(LocalDateTime.now())
                .totalKudos(sumKudos)
                .build();

        List<SkillProofInfo> skillProofInfos = new ArrayList<>();

        List<SkillKudosHistory> skillKudosHistories = postKudos.getPostKudosSkills().stream()
                .map(postKudosSkill -> {
                    Skill skill = skills.stream()
                            .filter(s -> s.getId().equals(postKudosSkill.getSkillId()))
                            .findFirst()
                            .orElse(null);

                    SkillKudos skillKudos = proof.getSkillKudos().stream()
                            .filter(skillKds -> skillKds.getSkill().getId().equals(skill.getId()))
                            .findFirst()
                            .orElse(null);

                    if (skillKudos == null) {
                        skillKudos = SkillKudos.builder()
                                .skill(skill)
                                .kudos(postKudosSkill.getKudos())
                                .build();
                        proof.getSkillKudos().add(skillKudos);
                    } else {
                        skillKudos.setKudos(skillKudos.getKudos() + postKudosSkill.getKudos());
                    }

                    SkillKudosHistory skillKudosHistory = SkillKudosHistory.builder()
                            .skill(skill)
                            .kudos(postKudosSkill.getKudos())
                            .kudosHistory(kudosHistory)
                            .build();

                    skillRepository.save(skill);
                    skillKudosRepository.save(skillKudos);
                    skillKudosHistoryRepository.save(skillKudosHistory);

                    skillProofInfos.add(new SkillProofInfo(skill.getId(), skill.getName(), skillKudos.getKudos()));

                    return skillKudosHistory;
                })
                .collect(Collectors.toList());

        kudosHistory.setSkillKudosHistories(skillKudosHistories);

        long currentCountKudos = proof.getKudos() + sumKudos;
        long currentBalance = sponsor.getKudos() - sumKudos;

        proof.setKudos(currentCountKudos);
        sponsor.setKudos(currentBalance);

        kudosHistoryRepository.save(kudosHistory);
        proofRepository.save(proof);
        sponsorRepository.save(sponsor);

        long currentSumKudos = kudosHistoryRepository.sumKudosProofBySponsorId(sponsorId, proofId);

        return new UpdatedProofKudos(currentCountKudos, currentSumKudos, currentBalance, skillProofInfos);
    }

    private void validatePositiveKudoses(PostKudos postKudos) {
        List<PostKudosSkill> postKudosSkills = postKudos.getPostKudosSkills().stream()
                .filter(pks -> pks.getKudos() < 1L)
                .toList();

        if (!postKudosSkills.isEmpty())
            throw new IllegalPostingKudos("Kudos should be positive");
    }

    private void validateProofContainSkills(Proof proof, Set<Skill> skills) {
        List<Skill> proofSkills = proof.getSkillKudos().stream()
                .map(SkillKudos::getSkill)
                .toList();
        if(!new HashSet<>(proofSkills).containsAll(skills)) {
            throw new ProofNotContainSkillException("Proof does not contain all skills");
        }
    }

    private Consumer<Proof> selectProofModifyStrategy(ProofModify proofModify,
                                                      Long proofId,
                                                      ContentStatus currentStatus) {
        Consumer<Proof> strategy;
        ContentStatus modifyingStatus = ContentStatus.valueOf(proofModify.getStatus());
        BiPredicate<ProofModify, ContentStatus> editCase = (proofEdit, status) ->
                modifyingStatus.equals(DRAFT) && status.equals(DRAFT);
        BiPredicate<ProofModify, ContentStatus> publishCase = (proofEdit, status) ->
                modifyingStatus.equals(PUBLISHED) && status.equals(DRAFT);
        BiPredicate<ProofModify, ContentStatus> hideCase = (proofEdit, status) ->
                modifyingStatus.equals(HIDDEN) && status.equals(PUBLISHED);
        BiPredicate<ProofModify, ContentStatus> reopenCase = (proofEdit, status) ->
                modifyingStatus.equals(PUBLISHED) && status.equals(HIDDEN);

        if (editCase.test(proofModify, currentStatus))
            strategy = proof -> updateProofData(proofModify, proof);
        else if (publishCase.test(proofModify, currentStatus))
            strategy = proof -> publishProof(proofModify, proof);
        else if (hideCase.test(proofModify, currentStatus))
            strategy = proof -> proof.setStatus(HIDDEN);
        else if (reopenCase.test(proofModify, currentStatus))
            strategy = proof -> proof.setStatus(PUBLISHED);
        else
            throw new IllegalContentModifyingException("Illegal operation for modifying status ["
                    + currentStatus + " -> " + proofModify.getStatus() + "]");

        return strategy;
    }

    private void updateProofData(ProofModify proofModify, Proof proof) {
        proof.setTitle(proofModify.getTitle());
        proof.setSummary(proofModify.getSummary());
        proof.setContent(proofModify.getContent());
        proof.setIconNumber(proofModify.getIconNumber());

        clearSkills(proof);
        setSkills(proofModify, proof);
    }

    private void publishProof(ProofModify proofModify, Proof proof) {
        if (proofModify.getSkillIds() != null && proofModify.getSkillIds().isEmpty()) {
            throw new IllegalContentModifyingException("Skills should be set for publishing");
        }
        updateProofData(proofModify, proof);
        proof.setPublished(LocalDateTime.now());
        proof.setStatus(PUBLISHED);
    }

    private void clearSkills(Proof proof) {
        skillKudosRepository.deleteAll(proof.getSkillKudos());
        if (proof.getSkillKudos() != null && !proof.getSkillKudos().isEmpty())
            proof.getSkillKudos().clear();
        proofRepository.save(proof);
    }

    private void verifyTalentExistsById(Long talentId) {
        if (!talentRepository.existsById(talentId)) {
            throw new TalentNotFoundException("Talent was not found");
        }
    }

    private void verifyProofExistsById(Long proofId) {
        if (!proofRepository.existsById(proofId)) {
            throw new ProofNotFoundException("Talent was not found");
        }
    }

    private void verifyTalentContainProof(Long talentId, Proof proof) {
        if(!hasTalentProof(talentId, proof)) {
            throw new UnrelatedContentException("This proof is not related to this talent's proofs");
        }
    }

    private boolean hasTalentProof(Long talentId, Proof proof) {
        return Objects.equals(proof.getTalent().getId(), talentId);
    }


    private Proof getProofById(Long id) {
        return proofRepository.findById(id)
                .orElseThrow(() -> new ProofNotFoundException("Proof was not found"));
    }

    private Talent getTalentById(Long id) {
        return talentRepository.findById(id)
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found"));
    }

    private Sponsor getSponsorById(Long id) {
        return sponsorRepository.findById(id)
                .orElseThrow(() -> new SponsorNotFoundException("Sponsor was not found"));
    }

    private void validatePostingKudos(Sponsor sponsor, Long sumKudos, Proof proof) {
        if (!proof.getStatus().equals(PUBLISHED))
            throw new ProofNotFoundException("Proof was not found");
        else if (sponsor.getKudos() - sumKudos < 0)
            throw new IllegalPostingKudos("You do not have balance for posting kudos");
        else if (KUDOS_MAX_VALUE - proof.getKudos() < sumKudos) {
            throw new IllegalPostingKudos("You reached max value of posting kudos");
        }
    }

    private Page<? extends ProofGeneralInfo> getPageProofsWithGeneralInfo(Long principalId,
                                                                          PageRequest pageRequest, String [] skills) {

        int skillsSize = (skills == null) ? 0 : skills.length;

        if (accessVerifyService.hasRole(SPONSOR)){
            Page<Object[]> proofsAndKudosSum = proofRepository
                    .findProofsAndKudosSumBySponsorId(principalId, PUBLISHED, pageRequest, skills, skillsSize);
            return mapper.toProofSponsorGeneralInfos(proofsAndKudosSum, pageRequest);
        }
        else if (accessVerifyService.hasRole(TALENT)){
            Page<Object[]> proofsAndIsMyProofList = proofRepository
                    .findProofsAndIsMyProofByTalentId(principalId, PUBLISHED, pageRequest, skills, skillsSize);
            return mapper.toProofTalentGeneralInfos(proofsAndIsMyProofList, pageRequest);
        }
        else
            return mapper.toProofGeneralInfos(proofRepository
                    .findAllByStatus(ContentStatus.PUBLISHED, pageRequest, skills, skillsSize));
    }

    private void validateGetTalentProofs(Long talentId, ContentStatus contentStatus) {
        verifyTalentExistsById(talentId);
        if (!PUBLISHED.equals(contentStatus))
            accessVerifyService.tryGetAccess(talentId, TALENT,
                    "You do not have permission to get list of proofs");

    }

    private Page<? extends ProofDetailInfo> getPageProofsWithDetailInfo(Long principalId,
                                                                        Long talentId,
                                                                        ContentStatus contentStatus,
                                                                        PageRequest pageRequest) {
        if (accessVerifyService.hasRole(SPONSOR)){
            Page<Object[]> talentProofs = proofRepository
                    .findAllTalentProofsBySponsorIdAndStatus(principalId, talentId, contentStatus, pageRequest);
            return mapper.toProofSponsorDetailInfos(talentProofs, pageRequest);
        }
        else{
            Page<Object[]> talentProofs = proofRepository
                    .findAllTalentProofsByTalentIdAndStatus(principalId, talentId, contentStatus, pageRequest);
            return mapper.toProofTalentDetailInfos(talentProofs, pageRequest);
        }
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

    private void setSkills(ProofModify proofModify, Proof proof) {
        Set<Long> skillsIds = new HashSet<>(proofModify.getSkillIds());
        int uniqueSkillIds = skillsIds.size();
        validateNotContainsDuplicates(uniqueSkillIds, proofModify.getSkillIds().size());

        Set<Skill> skills = new HashSet<>(skillRepository.findAllById(proofModify.getSkillIds()));
        if(uniqueSkillIds != skills.size()) {
            throw new SkillNotFoundException("Some skills which are not exist");
        }

        Set<SkillKudos> skillKudos = skills.stream()
                .map(sk-> SkillKudos.builder().skill(sk).proof(proof).kudos(0L).build())
                .collect(Collectors.toSet());


        proof.setSkillKudos(new HashSet<>(skillKudosRepository.saveAll(skillKudos)));

        proofRepository.save(proof);
    }

    private void validateNotContainsDuplicates(int uniqueSkillIds, int countSkillsIds) {
        if(uniqueSkillIds != countSkillsIds) {
            throw new DuplicateSkillException("Some skills have duplicates");
        }
    }
}
