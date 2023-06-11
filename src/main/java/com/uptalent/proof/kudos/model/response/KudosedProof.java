package com.uptalent.proof.kudos.model.response;

import com.uptalent.skill.model.SkillInfo;
import com.uptalent.util.model.response.Author;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class KudosedProof {
    private final Long proofId;
    private final Integer iconNumber;
    private final String title;
    private final Long totalSumKudos;
    private List<SkillInfo> skills;
    private final Author author;
}
