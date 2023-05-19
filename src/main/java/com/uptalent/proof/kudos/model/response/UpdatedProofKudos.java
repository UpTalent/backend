package com.uptalent.proof.kudos.model.response;

import com.uptalent.skill.model.SkillProofInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdatedProofKudos {
    private long currentCountKudos;
    private long currentSumKudosBySponsor;
    private long currentSponsorBalance;
    private List<SkillProofInfo> skills;
}
