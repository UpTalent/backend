package com.uptalent.proof.service;

import com.uptalent.mapper.ProofMapper;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.repository.ProofRepository;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.repository.TalentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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
