package com.uptalent.proof.kudos.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatedProofKudos {
    private long currentCountKudos;
    private long currentSumKudosBySponsor;
    private long currentSponsorBalance;
}
