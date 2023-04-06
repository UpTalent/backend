package com.uptalent.proof.controller;


import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.service.ProofService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class ProofController {
    private final ProofService proofService;

    @GetMapping("/talents/{talentId}/proofs/{proofId}")
    @ResponseStatus(HttpStatus.OK)
    public ProofDetailInfo getProofDetailInfo(@PathVariable Long talentId,
                                              @PathVariable Long proofId) {
        return proofService.getProofDetailInfo(talentId, proofId);
    }

    @PatchMapping("/talents/{talentId}/proofs/{proofId}")
    @ResponseStatus(HttpStatus.OK)
    public ProofDetailInfo editProof(@Valid @RequestBody ProofModify proofModify,
                                     @PathVariable Long talentId,
                                     @PathVariable Long proofId) {
        return proofService.editProof(proofModify, talentId, proofId);
    }

    @PostMapping("/talents/{talentId}/proofs")
    public ResponseEntity<?> createProof(@Valid @RequestBody ProofModify proofModify,
                                         @PathVariable Long talentId) {
        URI proofLocation = proofService.createProof(proofModify, talentId);

        return ResponseEntity.created(proofLocation).build();
    }
    @DeleteMapping("/talents/{talentId}/proofs/{proofId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteProof(@PathVariable Long proofId,
                                         @PathVariable Long talentId) {
        proofService.deleteProof(proofId, talentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

