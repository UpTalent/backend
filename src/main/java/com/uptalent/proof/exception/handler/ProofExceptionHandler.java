package com.uptalent.proof.exception.handler;

import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.exception.*;
import com.uptalent.proof.exception.WrongSortOrderException;
import com.uptalent.util.exception.handler.ExceptionHandlerController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProofExceptionHandler extends ExceptionHandlerController {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProofNotFoundException.class)
    public HttpResponse handlerProofNotFoundException(ProofNotFoundException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UnrelatedProofException.class)
    public HttpResponse handlerUnrelatedProofException(UnrelatedProofException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({IllegalProofModifyingException.class, IllegalCreatingProofException.class})
    public HttpResponse handlerIllegalProofModifyingException(RuntimeException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongSortOrderException.class)
    public HttpResponse handlerWrongSortOrderException(WrongSortOrderException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongStatusInputException.class)
    public HttpResponse handlerWrongStatusInputException(WrongStatusInputException e) {
        return new HttpResponse(e.getMessage());
    }
}
