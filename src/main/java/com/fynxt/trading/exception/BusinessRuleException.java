package com.fynxt.trading.exception;

/**
 * Thrown when a request violates a domain business rule
 * (e.g. pending order limit, insufficient holdings, invalid state transition).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
