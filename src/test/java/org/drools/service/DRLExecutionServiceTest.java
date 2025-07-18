package org.drools.service;

import org.drools.exception.DRLExecutionException;
import org.drools.execution.DRLRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DRLExecutionServiceTest {

    private DRLExecutionService executionService;

    @BeforeEach
    void setUp() {
        executionService = new DRLExecutionService();
    }

    @Test
    void testExecuteDRLWithJsonFacts_Success() {
        // Given
        String drlCode = "package org.example; rule \"Test\" when then end";
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        int maxActivations = 10;
        List<Object> expectedFacts = Arrays.asList("fact1", "fact2");

        // Mock the static method call
        try (MockedStatic<DRLRunner> mockedRunner = mockStatic(DRLRunner.class)) {
            mockedRunner.when(() -> DRLRunner.runDRLWithJsonFacts(drlCode, factsJson, maxActivations))
                       .thenReturn(expectedFacts);

            // When
            List<Object> result = executionService.executeDRLWithJsonFacts(drlCode, factsJson, maxActivations);

            // Then
            assertEquals(expectedFacts, result);
            mockedRunner.verify(() -> DRLRunner.runDRLWithJsonFacts(drlCode, factsJson, maxActivations));
        }
    }

    @Test
    void testExecuteDRLWithJsonFacts_NullCode() {
        // When & Then
        DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
            () -> executionService.executeDRLWithJsonFacts(null, "[]", 0));
        
        assertEquals("DRL code cannot be null or empty", exception.getMessage());
    }

    @Test
    void testExecuteDRLWithJsonFacts_EmptyCode() {
        // When & Then
        DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
            () -> executionService.executeDRLWithJsonFacts("", "[]", 0));
        
        assertEquals("DRL code cannot be null or empty", exception.getMessage());
    }

    @Test
    void testExecuteDRLWithJsonFacts_NegativeMaxActivations() {
        // When & Then
        DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
            () -> executionService.executeDRLWithJsonFacts("rule test when then end", "[]", -1));
        
        assertEquals("Maximum activations cannot be negative", exception.getMessage());
    }

    @Test
    void testExecuteDRLWithJsonFacts_ExecutionException() {
        // Given
        String drlCode = "package org.example; rule \"Test\" when then end";
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        int maxActivations = 10;
        RuntimeException executionException = new RuntimeException("Execution failed");

        // Mock the static method call to throw exception
        try (MockedStatic<DRLRunner> mockedRunner = mockStatic(DRLRunner.class)) {
            mockedRunner.when(() -> DRLRunner.runDRLWithJsonFacts(drlCode, factsJson, maxActivations))
                       .thenThrow(executionException);

            // When & Then
            DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
                () -> executionService.executeDRLWithJsonFacts(drlCode, factsJson, maxActivations));
            
            assertEquals("Failed to execute DRL: Execution failed", exception.getMessage());
            assertEquals(executionException, exception.getCause());
        }
    }

    @Test
    void testExecuteDRLWithFacts_Success() {
        // Given
        String drlCode = "package org.example; rule \"Test\" when then end";
        List<Object> facts = Arrays.asList("fact1", "fact2");
        int maxActivations = 5;
        List<Object> expectedResult = Arrays.asList("result1", "result2");

        // Mock the static method call
        try (MockedStatic<DRLRunner> mockedRunner = mockStatic(DRLRunner.class)) {
            mockedRunner.when(() -> DRLRunner.runDRLWithFacts(drlCode, facts, maxActivations))
                       .thenReturn(expectedResult);

            // When
            List<Object> result = executionService.executeDRLWithFacts(drlCode, facts, maxActivations);

            // Then
            assertEquals(expectedResult, result);
            mockedRunner.verify(() -> DRLRunner.runDRLWithFacts(drlCode, facts, maxActivations));
        }
    }

    @Test
    void testExecuteDRLWithFacts_NullCode() {
        // When & Then
        DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
            () -> executionService.executeDRLWithFacts(null, Arrays.asList(), 0));
        
        assertEquals("DRL code cannot be null or empty", exception.getMessage());
    }

    @Test
    void testExecuteDRLWithFacts_NegativeMaxActivations() {
        // When & Then
        DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
            () -> executionService.executeDRLWithFacts("rule test when then end", Arrays.asList(), -1));
        
        assertEquals("Maximum activations cannot be negative", exception.getMessage());
    }
}