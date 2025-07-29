package org.drools.service;

import org.drools.exception.DRLValidationException;
import org.drools.validation.DRLVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DRLValidationServiceTest {

    @Mock
    private DRLVerifier mockVerifier;
    
    private DRLValidationService validationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validationService = new DRLValidationService(mockVerifier);
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
    void testValidateDRLStructure_VerifierThrowsException() {
        // Given
        String drlCode = "invalid drl code";
        RuntimeException verifierException = new RuntimeException("Verifier error");
        when(mockVerifier.verify(drlCode)).thenThrow(verifierException);

        // When & Then
        DRLValidationException exception = assertThrows(DRLValidationException.class, 
            () -> validationService.validateDRLStructure(drlCode));
        
        assertEquals("Failed to validate DRL code: Verifier error", exception.getMessage());
        assertEquals(verifierException, exception.getCause());
        verify(mockVerifier).verify(drlCode);
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
}