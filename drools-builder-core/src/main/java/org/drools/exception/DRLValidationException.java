package org.drools.exception;

/**
 * Exception thrown when DRL validation fails.
 */
public class DRLValidationException extends RuntimeException {
    
    public DRLValidationException(String message) {
        super(message);
    }
    
    public DRLValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}