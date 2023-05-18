package com.uptalent.proof.kudos.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostKudosSkill {
    @Positive(message = "Number of kudos should be positive")
    private long kudos;

    @NotNull(message = "Skill id should not be null")
    private long skillId;
}
