package org.drools.exception;

/**
 * Exception thrown when a requested definition is not found.
 */
public class DefinitionNotFoundException extends RuntimeException {
    
    private final String definitionName;
    
    public DefinitionNotFoundException(String definitionName) {
        super("Definition with name '" + definitionName + "' not found");
        this.definitionName = definitionName;
    }
    
    public String getDefinitionName() {
        return definitionName;
    }
}