package com.deliveryflow.exception;

/** Thrown when a uniquely-constrained value (email, phone, vehicle number) already exists. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
