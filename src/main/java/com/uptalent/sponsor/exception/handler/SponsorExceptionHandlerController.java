package com.uptalent.sponsor.exception.handler;

import com.uptalent.payload.HttpResponse;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.util.exception.handler.ExceptionHandlerController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SponsorExceptionHandlerController extends ExceptionHandlerController {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SponsorNotFoundException.class)
    public HttpResponse handlerNotFoundSponsorException(SponsorNotFoundException e) {
        return new HttpResponse(e.getMessage());
    }
}
