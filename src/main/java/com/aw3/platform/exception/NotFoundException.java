package com.aw3.platform.exception;

/**
 * Exception for resource not found scenarios
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

