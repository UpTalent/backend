package com.uptalent.proof.controller;


import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofGeneralInfo;
import com.uptalent.proof.service.ProofService;
import com.uptalent.util.annotation.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
@Validated
public class ProofController {
    private final ProofService proofService;

    @GetMapping("/proofs")
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<ProofGeneralInfo> getAllProofs(
            @Min(value = 0, message = "Page should be greater or equals 0")
            @RequestParam(defaultValue = "0") int page,
            @Positive(message = "Size should be positive")
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "desc") String sort) {
        return proofService.getProofs(page, size, sort);
    }
    @GetMapping("/talents/{talent-id}/proofs")
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<ProofDetailInfo> getAllTalentProofs(
            @Min(value = 0, message = "Page should be greater or equals 0")
            @RequestParam(defaultValue = "0") int page,
            @Positive(message = "Size should be positive")
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "published")
            @EnumValue(enumClass = ProofStatus.class) String status,
            @RequestParam(defaultValue = "desc") String sort,
            @PathVariable("talent-id") Long talentId) {
        return proofService.getTalentProofs(page, size, sort, talentId, status);
    }
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

