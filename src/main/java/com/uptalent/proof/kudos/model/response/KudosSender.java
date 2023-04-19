package com.uptalent.proof.kudos.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class KudosSender {
    private String lastname;

    private String firstname;

    private String avatar;

    private LocalDateTime sent;

    private int kudos;
}
