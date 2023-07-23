package com.uptalent.answer.model.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackInfo {
    private Long id;
    private String contactInfo;
    private String message;
    private String status;
    private String title;
}
