package com.uptalent.vacancy.submission.exception;

public class SubmissionNotFoundException extends RuntimeException{
    public SubmissionNotFoundException(String message){
        super(message);
    }
}
