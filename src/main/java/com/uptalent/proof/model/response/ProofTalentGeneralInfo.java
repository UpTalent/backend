package com.uptalent.proof.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProofTalentGeneralInfo extends ProofGeneralInfo {
    private boolean myProof;

    public ProofTalentGeneralInfo(Long id, Integer iconNumber, String title,
                                  String summary, int kudos, LocalDateTime published, boolean myProof) {
        super(id, iconNumber, title, summary, kudos, published);
        this.myProof = myProof;
    }
}
