package com.techcorp.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String email) {
        super("Employee not found: " + email);
    }
}
