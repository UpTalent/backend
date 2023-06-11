package com.uptalent.proof.model.response;

import com.uptalent.skill.model.SkillProofInfo;
import com.uptalent.util.model.response.Author;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProofSponsorGeneralInfo extends ProofGeneralInfo {
    private long sumKudosFromMe;

    public ProofSponsorGeneralInfo(Long id, Integer iconNumber, String title,
                                   String summary, long kudos, LocalDateTime published, long sumKudosFromMe,
                                   Set<SkillProofInfo> skills, Author author) {
        super(id, iconNumber, title, summary, kudos, published, skills, author);
        this.sumKudosFromMe = sumKudosFromMe;
    }
}
