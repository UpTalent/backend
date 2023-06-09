package com.uptalent.vacancy.exception;

public class VacancyNotFoundException extends RuntimeException {
    public VacancyNotFoundException(String message) {
        super(message);
    }
}
