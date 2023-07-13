package com.uptalent.answer.model.request;

import com.uptalent.answer.model.enums.MessageStatus;
import com.uptalent.util.annotation.EnumValue;
import com.uptalent.vacancy.submission.model.enums.SubmissionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TemplateMessageRequest {
    @NotBlank(message = "Contact info should not be blank")
    @Size(max = 100, message = "Contact info must be less than 100 characters")
    private String contactInfo;
    @NotBlank(message = "Message should not be blank")
    @Size(max = 1000, message = "Message must be less than 1000 characters")
    private String message;
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    @EnumValue(enumClass = MessageStatus.class)
    private String status;
    @NotNull(message = "Is_templated_message should not be null")
    private Boolean isTemplatedMessage;
}
