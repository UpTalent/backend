package com.uptalent.proof.model.response;

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

    private LocalDateTime published;

    private ProofStatus status;
}
