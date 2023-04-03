package com.uptalent.proof.repository;

import com.uptalent.proof.model.entity.Proof;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProofRepository extends JpaRepository<Proof, Long> {
}
