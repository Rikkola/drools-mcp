package org.drools.service;

import org.drools.exception.DRLExecutionException;
import org.drools.execution.DRLPopulatorRunner;
import org.drools.execution.DRLRunnerResult;
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
        DRLRunnerResult expectedResult = new DRLRunnerResult(expectedFacts, 5);

        // Mock the static method call
        try (MockedStatic<DRLPopulatorRunner> mockedRunner = mockStatic(DRLPopulatorRunner.class)) {
            mockedRunner.when(() -> DRLPopulatorRunner.runDRLWithJsonFacts(drlCode, factsJson, maxActivations))
                       .thenReturn(expectedResult);

            // When
            DRLRunnerResult result = executionService.executeDRLWithJsonFacts(drlCode, factsJson, maxActivations);

            // Then
            assertEquals(expectedResult, result);
            assertEquals(expectedFacts, result.objects());
            assertEquals(5, result.firedRules());
            mockedRunner.verify(() -> DRLPopulatorRunner.runDRLWithJsonFacts(drlCode, factsJson, maxActivations));
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
        try (MockedStatic<DRLPopulatorRunner> mockedRunner = mockStatic(DRLPopulatorRunner.class)) {
            mockedRunner.when(() -> DRLPopulatorRunner.runDRLWithJsonFacts(drlCode, factsJson, maxActivations))
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
        List<Object> expectedFacts = Arrays.asList("result1", "result2");
        DRLRunnerResult expectedResult = new DRLRunnerResult(expectedFacts, 3);

        // Mock the static method call
        try (MockedStatic<DRLPopulatorRunner> mockedRunner = mockStatic(DRLPopulatorRunner.class)) {
            mockedRunner.when(() -> DRLPopulatorRunner.runDRLWithFacts(drlCode, facts, maxActivations))
                       .thenReturn(expectedResult);

            // When
            DRLRunnerResult result = executionService.executeDRLWithFacts(drlCode, facts, maxActivations);

            // Then
            assertEquals(expectedResult, result);
            assertEquals(expectedFacts, result.objects());
            assertEquals(3, result.firedRules());
            mockedRunner.verify(() -> DRLPopulatorRunner.runDRLWithFacts(drlCode, facts, maxActivations));
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

    @Test
    void testExecuteDRLWithJsonFactsAgainstStoredDefinitions_Success() {
        // Given
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        int maxActivations = 5;
        String expectedDRL = "package org.drools.generated; declare Person name: String age: int end";
        List<Object> expectedFacts = Arrays.asList("result1", "result2");
        DRLRunnerResult expectedResult = new DRLRunnerResult(expectedFacts, 2);
        
        DefinitionManagementService mockDefinitionService = mock(DefinitionManagementService.class);
        when(mockDefinitionService.generateDRLFromDefinitions("org.drools.generated")).thenReturn(expectedDRL);
        when(mockDefinitionService.getDefinitionCount()).thenReturn(1);

        // Mock the static method call
        try (MockedStatic<DRLPopulatorRunner> mockedRunner = mockStatic(DRLPopulatorRunner.class)) {
            mockedRunner.when(() -> DRLPopulatorRunner.runDRLWithJsonFacts(expectedDRL, factsJson, maxActivations))
                       .thenReturn(expectedResult);

            // When
            DRLRunnerResult result = executionService.executeDRLWithJsonFactsAgainstStoredDefinitions(factsJson, maxActivations, mockDefinitionService);

            // Then
            assertEquals(expectedResult, result);
            assertEquals(expectedFacts, result.objects());
            assertEquals(2, result.firedRules());
            verify(mockDefinitionService).generateDRLFromDefinitions("org.drools.generated");
            verify(mockDefinitionService).getDefinitionCount();
            mockedRunner.verify(() -> DRLPopulatorRunner.runDRLWithJsonFacts(expectedDRL, factsJson, maxActivations));
        }
    }

    @Test
    void testExecuteDRLWithJsonFactsAgainstStoredDefinitions_NoDefinitions() {
        // Given
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        int maxActivations = 5;
        
        DefinitionManagementService mockDefinitionService = mock(DefinitionManagementService.class);
        when(mockDefinitionService.generateDRLFromDefinitions("org.drools.generated")).thenReturn("package org.drools.generated;");
        when(mockDefinitionService.getDefinitionCount()).thenReturn(0);

        // When & Then
        DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
            () -> executionService.executeDRLWithJsonFactsAgainstStoredDefinitions(factsJson, maxActivations, mockDefinitionService));
        
        assertEquals("Failed to execute facts against stored definitions: No DRL definitions found in storage. Please add some definitions first using addDefinition.", exception.getMessage());
        verify(mockDefinitionService).generateDRLFromDefinitions("org.drools.generated");
        verify(mockDefinitionService).getDefinitionCount();
        verifyNoMoreInteractions(mockDefinitionService);
    }

    @Test
    void testExecuteDRLWithJsonFactsAgainstStoredDefinitions_NegativeMaxActivations() {
        // Given
        DefinitionManagementService mockDefinitionService = mock(DefinitionManagementService.class);

        // When & Then
        DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
            () -> executionService.executeDRLWithJsonFactsAgainstStoredDefinitions("[]", -1, mockDefinitionService));
        
        assertEquals("Maximum activations cannot be negative", exception.getMessage());
        verifyNoInteractions(mockDefinitionService);
    }

    @Test
    void testExecuteDRLWithJsonFactsAgainstStoredDefinitions_ExecutionException() {
        // Given
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        int maxActivations = 5;
        String expectedDRL = "package org.drools.generated; declare Person name: String age: int end";
        RuntimeException executionException = new RuntimeException("Execution failed");
        
        DefinitionManagementService mockDefinitionService = mock(DefinitionManagementService.class);
        when(mockDefinitionService.generateDRLFromDefinitions("org.drools.generated")).thenReturn(expectedDRL);
        when(mockDefinitionService.getDefinitionCount()).thenReturn(1);

        // Mock the static method call to throw exception
        try (MockedStatic<DRLPopulatorRunner> mockedRunner = mockStatic(DRLPopulatorRunner.class)) {
            mockedRunner.when(() -> DRLPopulatorRunner.runDRLWithJsonFacts(expectedDRL, factsJson, maxActivations))
                       .thenThrow(executionException);

            // When & Then
            DRLExecutionException exception = assertThrows(DRLExecutionException.class, 
                () -> executionService.executeDRLWithJsonFactsAgainstStoredDefinitions(factsJson, maxActivations, mockDefinitionService));
            
            assertEquals("Failed to execute facts against stored definitions: Execution failed", exception.getMessage());
            assertEquals(executionException, exception.getCause());
            verify(mockDefinitionService).generateDRLFromDefinitions("org.drools.generated");
            verify(mockDefinitionService).getDefinitionCount();
        }
    }
}