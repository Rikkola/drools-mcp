package org.drools.service;

import org.drools.exception.DefinitionNotFoundException;
import org.drools.storage.DefinitionStorage;
import java.util.List;

/**
 * Service responsible for managing DRL definitions.
 */
public class DefinitionManagementService {
    
    private final DefinitionStorage definitionStorage;
    
    public DefinitionManagementService() {
        this.definitionStorage = new DefinitionStorage();
    }
    
    public DefinitionManagementService(DefinitionStorage definitionStorage) {
        this.definitionStorage = definitionStorage;
    }
    
    /**
     * Adds a new definition or replaces an existing one.
     * 
     * @param name The name of the definition
     * @param type The type of definition (declare, function, etc.)
     * @param content The DRL content
     * @return The previous definition if it existed, null otherwise
     */
    public DefinitionStorage.DroolsDefinition addDefinition(String name, String type, String content) {
        validateDefinitionParams(name, type, content);
        return definitionStorage.addDefinition(name, type, content);
    }
    
    /**
     * Retrieves a definition by name.
     * 
     * @param name The name of the definition
     * @return The definition
     * @throws DefinitionNotFoundException if the definition doesn't exist
     */
    public DefinitionStorage.DroolsDefinition getDefinition(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition name cannot be null or empty");
        }
        
        DefinitionStorage.DroolsDefinition definition = definitionStorage.getDefinition(name);
        if (definition == null) {
            throw new DefinitionNotFoundException(name);
        }
        
        return definition;
    }
    
    /**
     * Retrieves all definitions.
     * 
     * @return List of all definitions
     */
    public List<DefinitionStorage.DroolsDefinition> getAllDefinitions() {
        return definitionStorage.getAllDefinitions();
    }
    
    /**
     * Removes a definition by name.
     * 
     * @param name The name of the definition to remove
     * @return The removed definition
     * @throws DefinitionNotFoundException if the definition doesn't exist
     */
    public DefinitionStorage.DroolsDefinition removeDefinition(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition name cannot be null or empty");
        }
        
        DefinitionStorage.DroolsDefinition removed = definitionStorage.removeDefinition(name);
        if (removed == null) {
            throw new DefinitionNotFoundException(name);
        }
        
        return removed;
    }
    
    /**
     * Generates a complete DRL string from all stored definitions.
     * 
     * @param packageName The package name to use (optional)
     * @return Complete DRL string
     */
    public String generateDRLFromDefinitions(String packageName) {
        return definitionStorage.generateDRLString(packageName);
    }
    
    /**
     * Gets the count of stored definitions.
     * 
     * @return Number of definitions
     */
    public int getDefinitionCount() {
        return definitionStorage.getDefinitionCount();
    }
    
    /**
     * Gets a summary of all definitions.
     * 
     * @return Summary string
     */
    public String getDefinitionsSummary() {
        return definitionStorage.getSummary();
    }
    
    /**
     * Validates definition parameters.
     */
    private void validateDefinitionParams(String name, String type, String content) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition name cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition type cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Definition content cannot be null or empty");
        }
    }
}