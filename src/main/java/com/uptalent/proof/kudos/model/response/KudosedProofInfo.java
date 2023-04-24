package com.uptalent.proof.kudos.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KudosedProofInfo {
    @JsonProperty(value = "id")
    private Long proofId;
    private Integer iconNumber;
    private String title;
    private int totalSumKudos;
}
