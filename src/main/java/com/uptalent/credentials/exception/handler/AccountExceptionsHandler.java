package com.uptalent.credentials.exception.handler;

import com.uptalent.credentials.exception.AccountExistsException;
import com.uptalent.payload.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountExceptionsHandler {
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AccountExistsException.class)
    public HttpResponse handlerExistsTalentException(AccountExistsException e) {
        return new HttpResponse(e.getMessage());
    }
}
