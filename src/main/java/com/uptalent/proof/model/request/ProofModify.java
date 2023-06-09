package com.uptalent.proof.model.request;

import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.util.annotation.EnumValue;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProofModify {
    @NotBlank(message = "Title should not be blank")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    @NotBlank(message = "Summary should not be blank")
    @Size(max = 255, message = "Summary must be less than 255 characters")
    private String summary;
    @NotBlank(message = "Content should not be blank")
    @Size(max = 5000, message = "Content must be less than 5000 characters")
    private String content;
    @NotNull(message = "Icon number should not be null")
    @Min(value = 0, message = "Icon number shouldn't be less than 0")
    private Integer iconNumber;
    @EnumValue(enumClass = ContentStatus.class)
    private String status;
    @Size(max=30, message = "List of skills should be less than 30 items")
    private List<Long> skillIds;
}
