package com.uptalent.proof.kudos.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KudosedProof {
    private Long proofId;
    private Integer iconNumber;
    private String title;
    private LocalDateTime sent;
    private int kudos;
}
