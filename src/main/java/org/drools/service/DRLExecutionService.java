package org.drools.service;

import org.drools.exception.DRLExecutionException;
import org.drools.execution.DRLRunner;
import java.util.List;

/**
 * Service responsible for DRL execution operations.
 */
public class DRLExecutionService {
    
    /**
     * Executes DRL code with external JSON facts.
     * 
     * @param drlCode The DRL code to execute
     * @param externalFactsJson JSON string containing external facts
     * @param maxActivations Maximum number of rule activations (0 for unlimited)
     * @return List of facts in working memory after execution
     * @throws DRLExecutionException if execution fails
     */
    public List<Object> executeDRLWithJsonFacts(String drlCode, String externalFactsJson, int maxActivations) {
        if (drlCode == null || drlCode.trim().isEmpty()) {
            throw new DRLExecutionException("DRL code cannot be null or empty");
        }
        
        if (maxActivations < 0) {
            throw new DRLExecutionException("Maximum activations cannot be negative");
        }
        
        try {
            return DRLRunner.runDRLWithJsonFacts(drlCode, externalFactsJson, maxActivations);
        } catch (Exception e) {
            throw new DRLExecutionException("Failed to execute DRL: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes DRL code with external facts.
     * 
     * @param drlCode The DRL code to execute
     * @param facts List of external facts to insert
     * @param maxActivations Maximum number of rule activations (0 for unlimited)
     * @return List of facts in working memory after execution
     * @throws DRLExecutionException if execution fails
     */
    public List<Object> executeDRLWithFacts(String drlCode, List<Object> facts, int maxActivations) {
        if (drlCode == null || drlCode.trim().isEmpty()) {
            throw new DRLExecutionException("DRL code cannot be null or empty");
        }
        
        if (maxActivations < 0) {
            throw new DRLExecutionException("Maximum activations cannot be negative");
        }
        
        try {
            return DRLRunner.runDRLWithFacts(drlCode, facts, maxActivations);
        } catch (Exception e) {
            throw new DRLExecutionException("Failed to execute DRL: " + e.getMessage(), e);
        }
    }

    /**
     * Executes external JSON facts against all stored DRL definitions.
     * 
     * @param externalFactsJson JSON string containing external facts
     * @param maxActivations Maximum number of rule activations (0 for unlimited)
     * @param definitionService Service to access stored definitions
     * @return List of facts in working memory after execution
     * @throws DRLExecutionException if execution fails
     */
    public List<Object> executeDRLWithJsonFactsAgainstStoredDefinitions(String externalFactsJson, int maxActivations, 
                                                                        DefinitionManagementService definitionService) {
        if (maxActivations < 0) {
            throw new DRLExecutionException("Maximum activations cannot be negative");
        }
        
        try {
            // Generate complete DRL from all stored definitions
            String drlCode = definitionService.generateDRLFromDefinitions("org.drools.generated");
            
            // Check if we have any definitions
            if (definitionService.getDefinitionCount() == 0) {
                throw new DRLExecutionException("No DRL definitions found in storage. Please add some definitions first using addDefinition.");
            }
            
            // Execute with the generated DRL
            return DRLRunner.runDRLWithJsonFacts(drlCode, externalFactsJson, maxActivations);
        } catch (Exception e) {
            throw new DRLExecutionException("Failed to execute facts against stored definitions: " + e.getMessage(), e);
        }
    }
}