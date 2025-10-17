package com.techcorp.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Pracownik z emailem '" + email + "' już istnieje");
    }
}
