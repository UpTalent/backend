package com.uptalent.vacancy.submission.exception;

public class DuplicateSubmissionException extends RuntimeException{
    public DuplicateSubmissionException(String message){
        super(message);
    }
}
