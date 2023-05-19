package com.uptalent.proof.kudos.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.skill.model.SkillInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
public class KudosedProofHistory {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sent;
    private long kudos;
    private Set<SkillInfo> skills;
}
