package com.uptalent.proof.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.skill.model.SkillProofInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProofGeneralInfo {
    private Long id;
    private Integer iconNumber;
    private String title;
    private String summary;
    private int kudos;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime published;
    private Set<SkillProofInfo> skills;
}
