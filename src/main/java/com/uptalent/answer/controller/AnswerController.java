package com.uptalent.answer.controller;

import com.uptalent.answer.model.request.TemplateMessageRequest;
import com.uptalent.answer.model.response.AnswerInfo;
import com.uptalent.answer.service.AnswerService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/answers")
@Validated
@Tag(name = "Answer", description = "Answer APIs documentation")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class AnswerController {
    private final AnswerService answerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void createTemplateMessage(@Valid @RequestBody TemplateMessageRequest templateMessageRequest){
        answerService.createTemplate(templateMessageRequest);
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    List<AnswerInfo> getAllTemplateMessage(){
        return answerService.getTemplates();
    }
}
