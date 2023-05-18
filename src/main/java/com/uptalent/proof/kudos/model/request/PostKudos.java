package com.uptalent.proof.kudos.model.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostKudos {
    @Size(max=30, message = "List of skills should be less than 30 items")
    private List<PostKudosSkill> postKudosSkills;
}
