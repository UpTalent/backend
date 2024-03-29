package com.uptalent.proof.model.response;

import com.uptalent.skill.model.SkillProofInfo;
import com.uptalent.util.model.response.Author;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ProofTalentGeneralInfo extends ProofGeneralInfo {
    private boolean myProof;

    public ProofTalentGeneralInfo(Long id, Integer iconNumber, String title,
                                  String summary, long kudos, LocalDateTime published, boolean myProof,
                                  Set<SkillProofInfo> skills, Author author) {
        super(id, iconNumber, title, summary, kudos, published, skills, author);
        this.myProof = myProof;
    }
}
