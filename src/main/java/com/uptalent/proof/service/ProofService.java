package com.uptalent.proof.service;

import com.uptalent.mapper.ProofMapper;
import com.uptalent.proof.exception.IllegalProofModifyingException;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.repository.TalentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
    private final ProofMapper mapper;

    public ProofDetailInfo getProofDetailInfo(Long talentId, Long proofId) {
        verifyTalentExistsById(talentId);

        Proof proof = getProofById(proofId);
        verifyTalentContainProof(talentId, proof);

        return mapper.toProofDetailInfo(proof);
    }

    @Transactional
    public ProofDetailInfo editProof(ProofModify proofModify, Long talentId, Long proofId) {
        verifyTalentExistsById(talentId);
        Proof foundProof = getProofById(proofId);
        verifyTalentContainProof(talentId, foundProof);

        Consumer<Proof> modifyingStrategy = selectProofModifyStrategy(proofModify, foundProof.getStatus());

        modifyingStrategy.accept(foundProof);

        return mapper.toProofDetailInfo(foundProof);
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
            strategy = proof -> publishProof(proof, modifyingStatus);
        else if (hideCase.test(proofModify, currentStatus))
            strategy = proof -> proof.setStatus(HIDDEN);
        else if (reopenCase.test(proofModify, currentStatus))
            strategy = proof -> proof.setStatus(PUBLISHED);
        else
            throw new IllegalProofModifyingException("Illegal operation for modifying status [" + currentStatus +
                    " -> " + proofModify.getStatus() + "]");

        return strategy;
    }

    private void updateProofData(ProofModify proofModify, Proof proof) {
        proof.setTitle(proofModify.getTitle());
        proof.setSummary(proofModify.getSummary());
        proof.setContent(proofModify.getContent());
        proof.setIconNumber(proofModify.getIconNumber());
    }

    private void publishProof(Proof proof, ProofStatus newStatus) {
        proof.setPublished(LocalDateTime.now());
        proof.setStatus(newStatus);
    }


    private void verifyTalentExistsById(Long talentId) {
        if (!talentRepository.existsById(talentId)) {
            throw new TalentNotFoundException("Talent was not found");
        }
    }

    private void verifyTalentContainProof(Long talentId, Proof proof) {
        if(!Objects.equals(proof.getTalent().getId(), talentId)) {
            throw new UnrelatedProofException("This proof is not related to this talent's proofs");
        }
    }

    private Proof getProofById(Long id) {
        return proofRepository.findById(id)
                .orElseThrow(() -> new ProofNotFoundException("Proof was not found"));
    }

}
