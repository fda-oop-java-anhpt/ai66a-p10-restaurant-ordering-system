package com.oop.project.exception;

/**
 * Thrown when a user tries to access a resource
 * without sufficient permission.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Unauthorized action");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
