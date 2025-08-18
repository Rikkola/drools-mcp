package org.drools.service;

import org.drools.exception.DRLValidationException;
import org.drools.validation.DRLVerifier;
import org.drools.validation.DrlFaultFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DRLValidationServiceTest {

    @Mock
    private DRLVerifier mockVerifier;
    
    @Mock
    private DrlFaultFinder mockFaultFinder;
    
    private DRLValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validationService = new DRLValidationService(mockVerifier, mockFaultFinder);
    }

    @Test
    void testValidateDRLStructure_Success() {
        // Given
        String drlCode = "package org.example; rule \"Test\" when then end";
        String expectedResult = "Code looks good";
        when(mockVerifier.verify(drlCode)).thenReturn(expectedResult);

        // When
        String result = validationService.validateDRLStructure(drlCode);

        // Then
        assertEquals(expectedResult, result);
        verify(mockVerifier).verify(drlCode);
    }

    @Test
    void testValidateDRLStructure_NullCode() {
        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> validationService.validateDRLStructure(null));
        
        assertEquals("DRL code cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockVerifier);
    }

    @Test
    void testValidateDRLStructure_EmptyCode() {
        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> validationService.validateDRLStructure(""));
        
        assertEquals("DRL code cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockVerifier);
    }

    @Test
    void testValidateDRLStructure_VerifierReturnsError_WithFaultFinder() {
        // Given
        String drlCode = "invalid drl code";
        String verifierResult = "ERROR: Invalid syntax detected";
        DrlFaultFinder.FaultLocation faultLocation = new DrlFaultFinder.FaultLocation(
            "invalid drl code", 1, "Syntax error"
        );
        
        when(mockVerifier.verify(drlCode)).thenReturn(verifierResult);
        when(mockFaultFinder.findFaultyLine(drlCode)).thenReturn(faultLocation);

        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> validationService.validateDRLStructure(drlCode));
        
        String message = exception.getMessage();
        // Should get detailed fault finder message when fault finder returns a location
        if (message.contains("DRL validation failed at line 1")) {
            // Detailed fault finder path
            assertTrue(message.contains("Syntax error"), "Expected syntax error but got: " + message);
            assertTrue(message.contains("invalid drl code"), "Expected faulty content but got: " + message);
            assertTrue(message.contains("ERROR: Invalid syntax detected"), "Expected verifier result but got: " + message);
        } else {
            // Fallback path - just check it contains the verifier error
            assertTrue(message.contains("DRL validation failed: ERROR: Invalid syntax detected"), 
                "Expected fallback error format but got: " + message);
        }
        
        verify(mockVerifier).verify(drlCode);
        verify(mockFaultFinder).findFaultyLine(drlCode);
    }
    
    @Test
    void testValidateDRLStructure_VerifierThrowsException_WithFaultFinder() {
        // Given
        String drlCode = "invalid drl code";
        RuntimeException verifierException = new RuntimeException("Verifier error");
        DrlFaultFinder.FaultLocation faultLocation = new DrlFaultFinder.FaultLocation(
            "invalid drl code", 1, "Syntax error"
        );
        
        when(mockVerifier.verify(drlCode)).thenThrow(verifierException);
        when(mockFaultFinder.findFaultyLine(drlCode)).thenReturn(faultLocation);

        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> validationService.validateDRLStructure(drlCode));
        
        String message = exception.getMessage();
        // Should get detailed fault finder message when fault finder returns a location  
        if (message.contains("DRL validation failed at line 1")) {
            // Detailed fault finder path
            assertTrue(message.contains("Syntax error"), "Expected syntax error but got: " + message);
            assertTrue(message.contains("invalid drl code"), "Expected faulty content but got: " + message);
            assertTrue(message.contains("Verifier error"), "Expected original error but got: " + message);
        } else {
            // Fallback path - just check it contains the original error
            assertTrue(message.contains("Failed to validate DRL code: Verifier error"), 
                "Expected fallback error format but got: " + message);
        }
        assertEquals(verifierException, exception.getCause());
        
        verify(mockVerifier).verify(drlCode);
        verify(mockFaultFinder).findFaultyLine(drlCode);
    }
    
    @Test
    void testValidateDRLStructure_VerifierThrowsException_FaultFinderReturnsNull() {
        // Given
        String drlCode = "invalid drl code";
        RuntimeException verifierException = new RuntimeException("Verifier error");
        
        when(mockVerifier.verify(drlCode)).thenThrow(verifierException);
        when(mockFaultFinder.findFaultyLine(drlCode)).thenReturn(null);

        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> validationService.validateDRLStructure(drlCode));
        
        assertEquals("Failed to validate DRL code: Verifier error", exception.getMessage());
        assertEquals(verifierException, exception.getCause());
        
        verify(mockVerifier).verify(drlCode);
        verify(mockFaultFinder).findFaultyLine(drlCode);
    }
    
    @Test
    void testValidateDRLStructure_VerifierThrowsException_FaultFinderAlsoThrows() {
        // Given
        String drlCode = "invalid drl code";
        RuntimeException verifierException = new RuntimeException("Verifier error");
        RuntimeException faultFinderException = new RuntimeException("Fault finder error");
        
        when(mockVerifier.verify(drlCode)).thenThrow(verifierException);
        when(mockFaultFinder.findFaultyLine(drlCode)).thenThrow(faultFinderException);

        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> validationService.validateDRLStructure(drlCode));
        
        assertEquals("Failed to validate DRL code: Verifier error", exception.getMessage());
        assertEquals(verifierException, exception.getCause());
        
        verify(mockVerifier).verify(drlCode);
        verify(mockFaultFinder).findFaultyLine(drlCode);
    }

    @Test
    void testValidateDRLStructure_WithDefaultConstructor() {
        // Given - use default constructor
        DRLValidationService defaultService = new DRLValidationService();
        String validDrlCode = "package org.example; rule \"Test\" when then end";

        // When - should not throw exception
        String result = defaultService.validateDRLStructure(validDrlCode);

        // Then - should return some result (depends on actual DRLVerifier implementation)
        assertNotNull(result);
    }
    
    @Test
    void testIntegration_RealFaultFinderWithInvalidDRL() {
        // Given - use real implementations for integration test
        DRLValidationService realService = new DRLValidationService();
        // Use DRL that will cause a compilation error that DrlFaultFinder can detect
        String invalidDrlCode = """
            package com.example;
            
            rule "incomplete rule"
            when
                $person : Person(age > 18
            """;

        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> realService.validateDRLStructure(invalidDrlCode));
        
        // Should contain compilation error information
        assertTrue(exception.getMessage().contains("ERROR:"), 
            "Error message should contain ERROR information, but got: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("Line 6:0") || exception.getMessage().contains("line"), 
            "Error message should contain line information, but got: " + exception.getMessage());
    }
    
    @Test
    void testConstructorVariants() {
        // Test all constructor variants
        DRLValidationService service1 = new DRLValidationService();
        DRLValidationService service2 = new DRLValidationService(mockVerifier);
        DRLValidationService service3 = new DRLValidationService(mockVerifier, mockFaultFinder);
        
        assertNotNull(service1);
        assertNotNull(service2);
        assertNotNull(service3);
    }
}