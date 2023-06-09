package com.uptalent.vacancy;

import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.util.annotation.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 3000, message = "Content must be less than 5000 characters")
    private String content;
    @EnumValue(enumClass = ContentStatus.class)
    private String status;
    @NotNull(message = "List skills should not be null")
    @Size(max=30, message = "List of skills should be less than 30 items")
    private List<Long> skillIds;
}
