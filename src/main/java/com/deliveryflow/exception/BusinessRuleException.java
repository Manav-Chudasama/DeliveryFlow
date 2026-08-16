package com.deliveryflow.exception;

/**
 * Thrown when a request is well-formed but violates a domain rule — assigning a driver who
 * is not available, or moving an order through an illegal status transition. Mapped to
 * HTTP 409 Conflict, which distinguishes "your input was malformed" (400) from "the system
 * is not in a state where this is allowed".
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
