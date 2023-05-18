package com.uptalent.skill.exception.handler;

import com.uptalent.payload.HttpResponse;
import com.uptalent.skill.exception.DuplicateSkillException;
import com.uptalent.skill.exception.SkillNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SkillExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SkillNotFoundException.class)
    public HttpResponse handlerSkillNotFoundException(SkillNotFoundException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateSkillException.class)
    public HttpResponse handlerDuplicateSkillException(DuplicateSkillException e) {
        return new HttpResponse(e.getMessage());
    }
}
