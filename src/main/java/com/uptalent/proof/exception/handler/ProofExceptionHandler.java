package com.uptalent.proof.exception.handler;

import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.exception.IllegalProofModifyingException;
import com.uptalent.proof.exception.ProofNotFoundException;
import com.uptalent.proof.exception.UnrelatedProofException;
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnrelatedProofException.class)
    public HttpResponse handlerUnrelatedProofException(UnrelatedProofException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalProofModifyingException.class)
    public HttpResponse handlerIllegalProofModifyingException(IllegalProofModifyingException e) {
        return new HttpResponse(e.getMessage());
    }
}
