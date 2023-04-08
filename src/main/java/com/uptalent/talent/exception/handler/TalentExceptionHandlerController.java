package com.uptalent.talent.exception.handler;

import com.uptalent.filestore.exception.FileStoreException;
import com.uptalent.payload.HttpResponse;
import com.uptalent.talent.exception.EmptySkillsException;
import com.uptalent.talent.exception.DeniedAccessException;
import com.uptalent.talent.exception.TalentExistsException;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.util.exception.handler.ExceptionHandlerController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class TalentExceptionHandlerController extends ExceptionHandlerController {

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
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public HttpResponse handlerBadCredentialsException(BadCredentialsException e) {
        return new HttpResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            FileStoreException.class,
            MaxUploadSizeExceededException.class,
            EmptySkillsException.class})
    public HttpResponse handlerFileStoreExceptions(RuntimeException e) {
        return new HttpResponse(e.getMessage());
    }

}
