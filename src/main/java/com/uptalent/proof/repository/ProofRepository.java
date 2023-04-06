package com.uptalent.proof.repository;

import com.uptalent.proof.model.entity.Proof;
import com.uptalent.proof.model.enums.ProofStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProofRepository extends JpaRepository<Proof, Long> {
    Page<Proof> findAllByStatus(ProofStatus published, PageRequest pageable);
    Page<Proof> findAllByTalentIdAndStatus(Long talentId, ProofStatus proofStatus, PageRequest of);
}
