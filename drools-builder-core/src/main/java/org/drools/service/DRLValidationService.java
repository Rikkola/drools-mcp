package org.drools.service;

import org.drools.exception.DRLValidationException;
import org.drools.validation.DRLVerifier;
import org.drools.validation.DrlFaultFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for DRL validation operations.
 */
public class DRLValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DRLValidationService.class);
    
    private final DRLVerifier verifier;
    private final DrlFaultFinder faultFinder;
    
    public DRLValidationService() {
        logger.debug("Initializing DRLValidationService with default DRLVerifier and DrlFaultFinder");
        this.verifier = new DRLVerifier();
        this.faultFinder = new DrlFaultFinder();
    }
    
    public DRLValidationService(DRLVerifier verifier) {
        logger.debug("Initializing DRLValidationService with custom DRLVerifier and default DrlFaultFinder");
        this.verifier = verifier;
        this.faultFinder = new DrlFaultFinder();
    }
    
    public DRLValidationService(DRLVerifier verifier, DrlFaultFinder faultFinder) {
        logger.debug("Initializing DRLValidationService with custom DRLVerifier and DrlFaultFinder");
        this.verifier = verifier;
        this.faultFinder = faultFinder;
    }
    
    /**
     * Validates DRL code structure and returns validation result.
     * 
     * @param drlCode The DRL code to validate
     * @return Validation result message
     * @throws DRLValidationException if validation fails
     */
    public String validateDRLStructure(String drlCode) {
        logger.info("Starting DRL validation for code with length: {}", 
            drlCode != null ? drlCode.length() : 0);
        
        if (drlCode == null || drlCode.trim().isEmpty()) {
            logger.warn("DRL validation failed: null or empty code provided");
            throw new DRLValidationException("DRL code cannot be null or empty");
        }
        
        logger.debug("DRL code to validate: {}{}", 
            drlCode.substring(0, Math.min(100, drlCode.length())),
            drlCode.length() > 100 ? "..." : "");
        
        try {
            logger.debug("Running DRLVerifier.verify()");
            String verificationResult = verifier.verify(drlCode);
            logger.debug("DRLVerifier result: {}", verificationResult);
            
            // Check if verifier found errors
            if (verificationResult.contains("ERROR:") || verificationResult.contains("WARNING:") || 
                verificationResult.contains("NOTE:")) {
                logger.info("DRLVerifier found errors, attempting to use DrlFaultFinder for precise location");
                // Use fault finder to pinpoint exact error location when verifier finds errors
                try {
                    DrlFaultFinder.FaultLocation faultLocation = faultFinder.findFaultyLine(drlCode);
                    if (faultLocation != null) {
                        logger.info("DrlFaultFinder located fault at line {}: {}", 
                            faultLocation.getLineNumber(), faultLocation.getErrorMessage());
                        String detailedError = String.format("DRL syntax error at line %d: %s%nFaulty content: %s%nVerifier result: %s",
                                faultLocation.getLineNumber(),
                                faultLocation.getErrorMessage(),
                                faultLocation.getFaultyContent(),
                                verificationResult);
                        logger.warn("DRL validation failed with detailed fault location: {}", detailedError);
                        throw new DRLValidationException(detailedError);
                    } else {
                        logger.warn("DrlFaultFinder could not locate specific fault, using verifier result only");
                    }
                } catch (Exception faultFinderException) {
                    logger.warn("DrlFaultFinder failed with exception: {}, falling back to verifier result", 
                        faultFinderException.getMessage());
                }
                
                logger.warn("DRL validation failed: {}", verificationResult);
                throw new DRLValidationException("DRL validation failed: " + verificationResult);
            }
            
            logger.info("DRL validation completed successfully");
            return verificationResult;
        } catch (DRLValidationException e) {
            logger.debug("Re-throwing DRLValidationException");
            // Re-throw DRLValidationException as-is
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected exception during DRL validation: {}", e.getMessage(), e);
            // Use fault finder for unexpected exceptions
            try {
                logger.debug("Attempting to use DrlFaultFinder for unexpected exception");
                DrlFaultFinder.FaultLocation faultLocation = faultFinder.findFaultyLine(drlCode);
                if (faultLocation != null) {
                    logger.info("DrlFaultFinder located fault at line {}: {}", 
                        faultLocation.getLineNumber(), faultLocation.getErrorMessage());
                    String detailedError = String.format("DRL syntax error at line %d: %s%nFaulty content: %s%nOriginal error: %s",
                            faultLocation.getLineNumber(),
                            faultLocation.getErrorMessage(),
                            faultLocation.getFaultyContent(),
                            e.getMessage());
                    logger.warn("DRL validation failed with detailed fault location from exception: {}", detailedError);
                    throw new DRLValidationException(detailedError, e);
                } else {
                    logger.warn("DrlFaultFinder could not locate specific fault for unexpected exception");
                }
            } catch (Exception faultFinderException) {
                logger.warn("DrlFaultFinder failed with exception during unexpected error handling: {}", 
                    faultFinderException.getMessage());
            }
            
            logger.error("Failed to validate DRL code: {}", e.getMessage());
            throw new DRLValidationException("Failed to validate DRL code: " + e.getMessage(), e);
        }
    }
}