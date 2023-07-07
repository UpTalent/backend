package com.uptalent.vacancy.exception.handler;

import com.uptalent.payload.HttpResponse;
import com.uptalent.util.exception.handler.ExceptionHandlerController;
import com.uptalent.vacancy.exception.NoSuchMatchedSkillsException;
import com.uptalent.vacancy.exception.VacancyNotFoundException;
import com.uptalent.vacancy.submission.exception.DuplicateSubmissionException;
import com.uptalent.vacancy.submission.exception.InvalidContactInfoException;
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

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(NoSuchMatchedSkillsException.class)
    public HttpResponse handlerNoSuchMatchedSkillsException(NoSuchMatchedSkillsException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateSubmissionException.class)
    public HttpResponse handlerDuplicateSubmissionException(DuplicateSubmissionException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidContactInfoException.class)
    public HttpResponse handlerInvalidContactInfoException(InvalidContactInfoException e) {
        return new HttpResponse(e.getMessage());
    }
}
