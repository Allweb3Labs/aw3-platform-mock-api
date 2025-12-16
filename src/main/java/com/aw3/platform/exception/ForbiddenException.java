package com.aw3.platform.exception;

/**
 * Exception for forbidden access scenarios
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

