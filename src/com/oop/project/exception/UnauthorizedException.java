package com.oop.project.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Unauthorized action");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
