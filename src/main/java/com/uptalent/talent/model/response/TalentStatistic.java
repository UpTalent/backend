package com.uptalent.talent.model.response;

import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofTalentDetailInfo;
import com.uptalent.skill.model.SkillInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class TalentStatistic {
    private long totalCountKudos;
    private Set<SkillInfo> mostKudosedSkills;
    private ProofTalentDetailInfo mostKudosedProof;
}
