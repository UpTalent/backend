package com.uptalent.proof.kudos.model.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostKudos {
    @Positive(message = "Number of kudos should be positive")
    private int kudos;
}
