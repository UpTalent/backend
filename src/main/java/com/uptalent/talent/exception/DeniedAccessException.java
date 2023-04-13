package com.uptalent.talent.exception;

public class DeniedAccessException extends RuntimeException{
    public DeniedAccessException(String message) {
        super(message);
    }
}