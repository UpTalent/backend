package com.uptalent.proof.service;

import com.uptalent.mapper.ProofMapper;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.proof.exception.*;
import com.uptalent.proof.kudos.exception.IllegalPostingKudos;
import com.uptalent.proof.kudos.model.entity.KudosHistory;
import com.uptalent.proof.kudos.model.response.KudosSender;
import com.uptalent.proof.kudos.repository.KudosHistoryRepository;
import com.uptalent.proof.kudos.model.request.PostKudos;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofGeneralInfo;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.util.service.AccessVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.uptalent.proof.model.enums.ProofStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProofService {
    private final ProofRepository proofRepository;
    private final TalentRepository talentRepository;
    private final KudosHistoryRepository kudosHistoryRepository;
    private final ProofMapper mapper;
    private final AccessVerifyService accessVerifyService;

    public PageWithMetadata<ProofGeneralInfo> getProofs(int page, int size, String sort) {
        Sort sortOrder = getSortByString(sort, PUBLISHED);
        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);

        Long principalTalentId = accessVerifyService.getPrincipalId();


        Page<Proof> proofsPage = proofRepository.findAllByStatus(ProofStatus.PUBLISHED, pageRequest);
        List<Proof> retrievedProofs = proofsPage.getContent();

        if (principalTalentId != 0L) {
            Talent talent = getTalentById(principalTalentId);
            List<Proof> kudosedProofs = talent.getKudosHistory().stream().map(KudosHistory::getProof).toList();

            retrievedProofs.forEach(rp -> rp.setKudosedByMe(kudosedProofs.contains(rp)));
        }

        List<ProofGeneralInfo> proofGeneralInfos = mapper.toProofGeneralInfos(retrievedProofs);

        return new PageWithMetadata<>(proofGeneralInfos, proofsPage.getTotalPages());
    }

    public ProofDetailInfo getProofDetailInfo(Long talentId, Long proofId) {
        verifyTalentExistsById(talentId);
        accessVerifyService.tryGetAccess(talentId, "You cannot get proof detail info");
        Proof proof = getProofById(proofId);

        verifyTalentContainProof(talentId, proof);

        return mapper.toProofDetailInfo(proof);
    }

    @Transactional
    public URI createProof(ProofModify proofModify, Long talentId) {
        Talent talent = getTalentById(talentId);

        accessVerifyService.tryGetAccess(talentId, "You do not have permission to create proof");

        Proof proof = mapper.toProof(proofModify);

        if (!proof.getStatus().equals(DRAFT))
            throw new IllegalCreatingProofException("Proof status for creating should be DRAFT");

        proof.setTalent(talent);
        proofRepository.save(proof);

        talent.getProofs().add(proof);
        talentRepository.save(talent);

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(proof.getId())
                .toUri();
    }

    @Transactional
    public ProofDetailInfo editProof(ProofModify proofModify, Long talentId, Long proofId) {
        verifyTalentExistsById(talentId);
        accessVerifyService.tryGetAccess(talentId, "You do not have permission to edit proof");
        Proof foundProof = getProofById(proofId);
        verifyTalentContainProof(talentId, foundProof);

        Consumer<Proof> modifyingStrategy = selectProofModifyStrategy(proofModify, foundProof.getStatus());

        modifyingStrategy.accept(foundProof);

        return mapper.toProofDetailInfo(foundProof);
    }

    public PageWithMetadata<ProofDetailInfo> getTalentProofs(int page, int size, String sort,
                                                             Long talentId, String status) {
        verifyTalentExistsById(talentId);
        ProofStatus proofStatus = ProofStatus.valueOf(status.toUpperCase());

        if (!PUBLISHED.equals(proofStatus))
            accessVerifyService.tryGetAccess(talentId, "You do not have permission to get list of proofs");

        Sort sortOrder = getSortByString(sort, proofStatus);
        PageRequest pageRequest = PageRequest.of(page, size, sortOrder);

        Page<Proof> proofsPage = proofRepository.findAllByTalentIdAndStatus(talentId, proofStatus,
                PageRequest.of(page, size, sortOrder));

        Long principalTalentId = accessVerifyService.getPrincipalId();
        List<Proof> retrievedProofs = proofsPage.getContent();

        if (principalTalentId != 0L) {
            Talent talent = getTalentById(principalTalentId);
            List<Proof> kudosedProofs = talent.getKudosHistory().stream().map(KudosHistory::getProof).toList();

            retrievedProofs.forEach(rp -> rp.setKudosedByMe(kudosedProofs.contains(rp)));
        }

        List<ProofDetailInfo> proofDetailInfos = mapper.toProofDetailInfos(proofsPage.getContent());

        return new PageWithMetadata<>(proofDetailInfos, proofsPage.getTotalPages());
    }

    @Transactional
    public void deleteProof(Long proofId, Long talentId) {
        verifyTalentExistsById(talentId);
        accessVerifyService.tryGetAccess(talentId, "You do not have permission to delete proof");

        Proof proofToDelete = getProofById(proofId);
        verifyTalentContainProof(talentId, proofToDelete);

        proofRepository.delete(proofToDelete);
    }

    public List<KudosSender> getKudosSenders(Long proofId) {
        verifyProofExistsById(proofId);
        verifyTalentContainProof(accessVerifyService.getPrincipalId(), getProofById(proofId));
        return kudosHistoryRepository.findKudosSendersByProofId(proofId);
    }

    @Transactional
    public void postKudos(PostKudos kudos, Long proofId) {
        Long talentId = accessVerifyService.getPrincipalId();
        Proof proof = getProofById(proofId);

        validatePostingKudos(talentId, proof);

        Talent talent = getTalentById(talentId);
        KudosHistory kudosHistory = KudosHistory.builder()
                .talent(talent)
                .proof(proof)
                .sent(LocalDateTime.now())
                .kudos(kudos.getKudos())
                .build();

        proof.setKudos(proof.getKudos() + kudos.getKudos());

        kudosHistoryRepository.save(kudosHistory);
        proofRepository.save(proof);
    }


    private Consumer<Proof> selectProofModifyStrategy(ProofModify proofModify, ProofStatus currentStatus) {
        Consumer<Proof> strategy;
        ProofStatus modifyingStatus = ProofStatus.valueOf(proofModify.getStatus());
        BiPredicate<ProofModify, ProofStatus> editCase = (proofEdit, status) ->
                modifyingStatus.equals(DRAFT) && status.equals(DRAFT);
        BiPredicate<ProofModify, ProofStatus> publishCase = (proofEdit, status) ->
                modifyingStatus.equals(PUBLISHED) && status.equals(DRAFT);
        BiPredicate<ProofModify, ProofStatus> hideCase = (proofEdit, status) ->
                modifyingStatus.equals(HIDDEN) && status.equals(PUBLISHED);
        BiPredicate<ProofModify, ProofStatus> reopenCase = (proofEdit, status) ->
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
            throw new IllegalProofModifyingException("Illegal operation for modifying status ["
                    + currentStatus + " -> " + proofModify.getStatus() + "]");

        return strategy;
    }

    private void updateProofData(ProofModify proofModify, Proof proof) {
        proof.setTitle(proofModify.getTitle());
        proof.setSummary(proofModify.getSummary());
        proof.setContent(proofModify.getContent());
        proof.setIconNumber(proofModify.getIconNumber());
    }

    private void publishProof(ProofModify proofModify, Proof proof) {
        updateProofData(proofModify, proof);
        proof.setPublished(LocalDateTime.now());
        proof.setStatus(PUBLISHED);
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
            throw new UnrelatedProofException("This proof is not related to this talent's proofs");
        }
    }

    private boolean hasTalentProof(Long talentId, Proof proof) {
        return Objects.equals(proof.getTalent().getId(), talentId);
    }

    private boolean isPostedKudosBefore(Long talentId, Proof proof) {
        return kudosHistoryRepository.pressedProofByTalentId(talentId, proof.getId());
    }

    private Proof getProofById(Long id) {
        return proofRepository.findById(id)
                .orElseThrow(() -> new ProofNotFoundException("Proof was not found"));
    }

    private Talent getTalentById(Long id) {
        return talentRepository.findById(id)
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found"));
    }

    private void validatePostingKudos(Long talentId, Proof proof) {
        if (hasTalentProof(talentId, proof))
            throw new IllegalPostingKudos("You cannot post kudos to own proof");
        else if (!proof.getStatus().equals(PUBLISHED))
            throw new ProofNotFoundException("Proof was not found");
        else if (isPostedKudosBefore(talentId, proof))
            throw new IllegalPostingKudos("You cannot post kudos again");
    }


    private Sort getSortByString(String sort, ProofStatus status){
        String sortField = status.equals(DRAFT) ? "id" : "published";

        if(sort.equals("desc"))
            return Sort.by(sortField).descending();
        else if (sort.equals("asc"))
            return Sort.by(sortField).ascending();
        else
            throw new WrongSortOrderException("Unexpected input of sort order");
    }
}
