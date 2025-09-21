package com.gonga.tcc.microsservice.exceptions;

/**
 * Exception thrown when a requested resource is not found in the system.
 * Use this exception to indicate that a specific entity or data item could not be located.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}