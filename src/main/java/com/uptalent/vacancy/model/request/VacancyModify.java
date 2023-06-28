package com.uptalent.vacancy.model.request;

import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.util.annotation.EnumValue;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VacancyModify {
    @NotBlank(message = "Title should not be blank")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    @NotBlank(message = "Content should not be blank")
    @Size(max = 5000, message = "Content must be less than 5000 characters")
    private String content;
    @EnumValue(enumClass = ContentStatus.class)
    private String status;
    @NotNull(message = "List skills should not be null")
    @Size(max=30, message = "List of skills should be less than 30 items")
    private List<Long> skillIds;
    @Min(value = 50, message = "Min percent of matched skills should be 50%")
    @Max(value = 100, message = "Max percent of matched skills should be 100%")
    private Integer skillsMatchedPercent;
}
