package com.techcorp.exception;

public class FileMissingException extends RuntimeException {
    public FileMissingException(String message) {
        super(message);
    }
}
