package com.uptalent.talent.model.exception;


public class TalentNotFoundException extends RuntimeException {
    public TalentNotFoundException(String message) {
        super(message);
    }
}
