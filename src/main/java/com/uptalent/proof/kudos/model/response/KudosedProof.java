package com.uptalent.proof.kudos.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uptalent.skill.model.SkillInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class KudosedProof {
    private final Long proofId;
    private final Integer iconNumber;
    private final String title;
    private final Long totalSumKudos;
    private List<SkillInfo> skills;

}
