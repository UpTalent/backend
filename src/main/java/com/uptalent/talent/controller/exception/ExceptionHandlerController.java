package com.uptalent.talent.controller.exception;

import com.uptalent.filestore.exception.FileStoreException;
import com.uptalent.payload.HttpResponse;
import com.uptalent.talent.model.exception.DeniedAccessException;
import com.uptalent.talent.model.exception.TalentExistsException;
import com.uptalent.talent.model.exception.TalentNotFoundException;
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
import java.util.HashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler{

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TalentNotFoundException.class)
    public HttpResponse handlerNotFoundTalentException(TalentNotFoundException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(TalentExistsException.class)
    public HttpResponse handlerExistsTalentException(TalentExistsException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(DeniedAccessException.class)
    public HttpResponse handlerExistsTalentException(DeniedAccessException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FileStoreException.class)
    public HttpResponse handlerFileStoreExceptions(FileStoreException e) {
        return new HttpResponse(e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, status);
    }
}
