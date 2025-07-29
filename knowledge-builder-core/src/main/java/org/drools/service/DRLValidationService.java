package org.drools.service;

import org.drools.exception.DRLValidationException;
import org.drools.validation.DRLVerifier;

/**
 * Service responsible for DRL validation operations.
 */
public class DRLValidationService {
    
    private final DRLVerifier verifier;
    
    public DRLValidationService() {
        this.verifier = new DRLVerifier();
    }
    
    public DRLValidationService(DRLVerifier verifier) {
        this.verifier = verifier;
    }
    
    /**
     * Validates DRL code structure and returns validation result.
     * 
     * @param drlCode The DRL code to validate
     * @return Validation result message
     * @throws DRLValidationException if validation fails
     */
    public String validateDRLStructure(String drlCode) {
        if (drlCode == null || drlCode.trim().isEmpty()) {
            throw new DRLValidationException("DRL code cannot be null or empty");
        }
        
        try {
            return verifier.verify(drlCode);
        } catch (Exception e) {
            throw new DRLValidationException("Failed to validate DRL code: " + e.getMessage(), e);
        }
    }
}