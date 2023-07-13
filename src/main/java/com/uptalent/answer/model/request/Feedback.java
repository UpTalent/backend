package com.uptalent.answer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uptalent.answer.model.enums.MessageStatus;
import com.uptalent.util.annotation.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Feedback {
    @JsonProperty(value = "contact_info")
    @NotBlank(message = "Contact info should not be blank")
    @Size(max = 100, message = "Contact info must be less than 100 characters")
    private String contactInfo;
    @NotBlank(message = "Message should not be blank")
    @Size(max = 1000, message = "Message must be less than 1000 characters")
    private String message;
    @EnumValue(enumClass = MessageStatus.class)
    private MessageStatus status;
}
