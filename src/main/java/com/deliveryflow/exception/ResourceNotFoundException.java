package com.deliveryflow.exception;

/** Thrown when a requested entity does not exist. Mapped to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super("%s not found with id: %d".formatted(resource, id));
    }
}
