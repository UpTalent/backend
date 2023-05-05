package com.uptalent.proof.model.response;

import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.skill.model.entity.Skill;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProofTalentDetailInfo extends ProofDetailInfo {
    private boolean myProof;

    public ProofTalentDetailInfo(Long id, Integer iconNumber, String title, String summary, String content,
                                 LocalDateTime published, int kudos, ProofStatus status, boolean myProof,
                                 Set<Skill> skills) {
        super(id, iconNumber, title, summary, content, published, kudos, status, skills);
        this.myProof = myProof;
    }
}
