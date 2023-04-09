package com.uptalent.util.exception.handler;

import com.uptalent.payload.HttpResponse;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.util.exception.InvalidEnumValueException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, status);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidEnumValueException.class)
    public HttpResponse handlerInvalidEnumValueException(InvalidEnumValueException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(DeniedAccessException.class)
    public HttpResponse handlerExistsTalentException(DeniedAccessException e) {
        return new HttpResponse(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handlerConstraintViolationException(ConstraintViolationException e) {
        String [] errorMessages = e.getMessage().split(", ");
        Map<String, String> errors = new HashMap<>();
        Arrays.stream(errorMessages).forEach(
                error -> errors.put(error.split("\\.")[1].split(": ")[0], error.split(": ")[1]));

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


}
