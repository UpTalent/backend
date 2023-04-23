package com.uptalent.proof.model.response;

import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProofSponsorGeneralInfo extends ProofGeneralInfo {
    private boolean kudosedByMe;

    public ProofSponsorGeneralInfo(Long id, Integer iconNumber, String title,
                                   String summary, int kudos, LocalDateTime published, boolean kudosedByMe) {
        super(id, iconNumber, title, summary, kudos, published);
        this.kudosedByMe = kudosedByMe;
    }
}
