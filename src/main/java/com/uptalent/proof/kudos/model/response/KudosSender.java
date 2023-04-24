package com.uptalent.proof.kudos.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String fullname;
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sent;
    private int kudos;
}
