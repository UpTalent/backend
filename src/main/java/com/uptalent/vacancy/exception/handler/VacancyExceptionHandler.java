package com.uptalent.vacancy.exception.handler;

import com.uptalent.payload.HttpResponse;
import com.uptalent.util.exception.handler.ExceptionHandlerController;
import com.uptalent.vacancy.exception.IllegalVacancyModifyingException;
import com.uptalent.vacancy.exception.VacancyNotFoundException;
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
