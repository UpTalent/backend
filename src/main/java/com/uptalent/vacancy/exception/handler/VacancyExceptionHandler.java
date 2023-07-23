package com.uptalent.vacancy.exception.handler;

import com.uptalent.answer.exception.FeedbackNotFoundException;
import com.uptalent.payload.HttpResponse;
import com.uptalent.util.exception.handler.ExceptionHandlerController;
import com.uptalent.vacancy.exception.NoSuchMatchedSkillsException;
import com.uptalent.vacancy.exception.VacancyNotFoundException;
import com.uptalent.vacancy.submission.exception.DuplicateSubmissionException;
import com.uptalent.vacancy.submission.exception.IllegalSubmissionException;
import com.uptalent.vacancy.submission.exception.InvalidContactInfoException;
import com.uptalent.vacancy.submission.exception.SubmissionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class VacancyExceptionHandler extends ExceptionHandlerController {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({VacancyNotFoundException.class,
            SubmissionNotFoundException.class,
            FeedbackNotFoundException.class})
    public HttpResponse handlerVacancyNotFoundException(RuntimeException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(NoSuchMatchedSkillsException.class)
    public HttpResponse handlerNoSuchMatchedSkillsException(NoSuchMatchedSkillsException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({DuplicateSubmissionException.class,
            IllegalSubmissionException.class})
    public HttpResponse handlerDuplicateSubmissionException(RuntimeException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidContactInfoException.class)
    public HttpResponse handlerInvalidContactInfoException(InvalidContactInfoException e) {
        return new HttpResponse(e.getMessage());
    }
}
