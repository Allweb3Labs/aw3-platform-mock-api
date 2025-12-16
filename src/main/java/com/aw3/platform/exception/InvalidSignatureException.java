package com.aw3.platform.exception;

/**
 * Exception for invalid signature scenarios
 */
public class InvalidSignatureException extends RuntimeException {
    public InvalidSignatureException(String message) {
        super(message);
    }
}

