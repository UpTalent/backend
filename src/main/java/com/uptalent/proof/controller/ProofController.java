package com.uptalent.proof.controller;

import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.service.ProofService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
