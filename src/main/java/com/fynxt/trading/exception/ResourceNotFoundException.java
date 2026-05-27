package com.fynxt.trading.exception;

/**
 * Thrown when a requested resource (order, trader, position) does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
