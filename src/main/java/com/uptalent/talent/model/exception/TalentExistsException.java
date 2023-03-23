package com.uptalent.talent.model.exception;

public class TalentExistsException extends RuntimeException {
    public TalentExistsException(String message) {
        super(message);
    }
}
