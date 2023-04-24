package com.uptalent.proof.kudos.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KudosedProofHistory {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sent;
    private int kudos;
}
