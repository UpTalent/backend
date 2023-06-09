package com.uptalent.vacancy;

import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.exception.*;
import com.uptalent.proof.kudos.exception.IllegalPostingKudos;
import com.uptalent.util.exception.handler.ExceptionHandlerController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class VacancyExceptionHandler extends ExceptionHandlerController {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(VacancyNotFoundException.class)
    public HttpResponse handlerVacancyNotFoundException(VacancyNotFoundException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalVacancyModifyingException.class)
    public HttpResponse handlerConflictException(RuntimeException e) {
        return new HttpResponse(e.getMessage());
    }

}
