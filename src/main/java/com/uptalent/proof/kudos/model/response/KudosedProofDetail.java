package com.uptalent.proof.kudos.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class KudosedProofDetail {
    private KudosedProofInfo proofInfo;
    private List<KudosedProofHistory> histories;
}
