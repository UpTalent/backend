package com.uptalent.proof.kudos.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KudosedProof {
    private Long proofId;
    private Integer iconNumber;
    private String title;
    private Long totalSumKudos;
}
