package com.uptalent.proof.model.response;

import com.uptalent.proof.model.enums.ProofStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProofSponsorDetailInfo extends ProofDetailInfo {
    private boolean kudosedByMe;

    public ProofSponsorDetailInfo(Long id, Integer iconNumber, String title, String summary, String content,
                                  LocalDateTime published, int kudos, ProofStatus status, boolean kudosedByMe) {
        super(id, iconNumber, title, summary, content, published, kudos, status);
        this.kudosedByMe = kudosedByMe;
    }
}
