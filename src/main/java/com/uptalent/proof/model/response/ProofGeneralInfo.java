package com.uptalent.proof.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProofGeneralInfo {
    private Long id;
    private Integer iconNumber;
    private String title;
    private String summary;
    private int kudos;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime published;
}
