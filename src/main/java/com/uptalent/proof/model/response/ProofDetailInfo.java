package com.uptalent.proof.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.skill.model.SkillProofInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProofDetailInfo {
    private Long id;
    private Integer iconNumber;
    private String title;
    private String summary;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime published;
    private long kudos;
    private ProofStatus status;
    private Set<SkillProofInfo> skills;
}
