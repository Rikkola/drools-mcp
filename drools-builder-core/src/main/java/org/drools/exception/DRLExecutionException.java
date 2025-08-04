package org.drools.exception;

/**
 * Exception thrown when DRL execution fails.
 */
public class DRLExecutionException extends RuntimeException {
    
    public DRLExecutionException(String message) {
        super(message);
    }
    
    public DRLExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}