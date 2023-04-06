package com.uptalent.proof.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProofGeneralInfo {
    private Long id;

    private Integer iconNumber;

    private String title;

    private String summary;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime published;
}
