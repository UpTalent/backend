package com.uptalent.proof.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uptalent.proof.model.enums.ProofStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProofDetailInfo {
    private Long id;

    private Integer iconNumber;

    private String title;

    private String summary;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime published;

    private ProofStatus status;
}
