package com.uptalent.proof.model.response;

import com.uptalent.skill.model.SkillInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProofSponsorGeneralInfo extends ProofGeneralInfo {
    private long sumKudosFromMe;

    public ProofSponsorGeneralInfo(Long id, Integer iconNumber, String title,
                                   String summary, int kudos, LocalDateTime published, long sumKudosFromMe,
                                   Set<SkillInfo> skills) {
        super(id, iconNumber, title, summary, kudos, published, skills);
        this.sumKudosFromMe = sumKudosFromMe;
    }
}
