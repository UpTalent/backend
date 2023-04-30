package com.uptalent.proof.model.response;

import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProofSponsorGeneralInfo extends ProofGeneralInfo {
    private long sumKudosFromMe;

    public ProofSponsorGeneralInfo(Long id, Integer iconNumber, String title,
                                   String summary, int kudos, LocalDateTime published, long sumKudosFromMe) {
        super(id, iconNumber, title, summary, kudos, published);
        this.sumKudosFromMe = sumKudosFromMe;
    }
}
