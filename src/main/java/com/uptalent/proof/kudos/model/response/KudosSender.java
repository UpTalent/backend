package com.uptalent.proof.kudos.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.skill.model.SkillInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class KudosSender {
    private String fullname;
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sent;
    private long kudos;
    private Set<SkillInfo> skills;
}
