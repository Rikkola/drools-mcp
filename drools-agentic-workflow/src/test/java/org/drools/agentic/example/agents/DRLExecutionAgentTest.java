package org.drools.agentic.example.agents;

import org.drools.exception.DRLExecutionException;
import org.drools.execution.DRLRunnerResult;
import org.drools.service.DRLExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DRLExecutionAgentTest {

    @Mock
    private DRLExecutionService mockExecutionService;

    private DRLExecutionAgent agent;

    private static final String SAMPLE_DRL = """
            package com.example
            
            declare Person
                name : String
                age : int
            end
            
            rule "Adult Check"
            when
                $p : Person(age >= 18)
            then
                System.out.println("Adult: " + $p.getName());
            end
            """;

    private static final String SAMPLE_JSON = """
            [{"_type":"Person", "name":"John", "age":25}]
            """;

    @BeforeEach
    void setUp() throws Exception {
        // Create agent and inject mock using reflection
        agent = new DRLExecutionAgent();
        Field executionServiceField = DRLExecutionAgent.class.getDeclaredField("executionService");
        executionServiceField.setAccessible(true);
        executionServiceField.set(agent, mockExecutionService);
        
        // Reset the mock before each test
        reset(mockExecutionService);
    }

    @Test
    void testValidateDRL_SuccessfulExecution() throws DRLExecutionException {
        // Given
        DRLRunnerResult result = new DRLRunnerResult(Arrays.asList("Adult Check"), 1);
        when(mockExecutionService.executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100))
                .thenReturn(result);

        // When
        String response = agent.validateDRL("test-memory-id", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        assertEquals("Code looks good", response);
        verify(mockExecutionService).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }

    @Test
    void testValidateDRL_NoRulesFired() throws DRLExecutionException {
        // Given
        DRLRunnerResult result = new DRLRunnerResult(Arrays.asList(), 0);
        when(mockExecutionService.executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100))
                .thenReturn(result);

        // When
        String response = agent.validateDRL("test-memory-id", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        assertEquals("None of the rules fired with the given test data.", response);
        verify(mockExecutionService).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }

    @Test
    void testValidateDRL_ExecutionException() throws DRLExecutionException {
        // Given
        when(mockExecutionService.executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100))
                .thenThrow(new DRLExecutionException("Compilation error: Unknown type Person"));

        // When
        String response = agent.validateDRL("test-memory-id", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        assertEquals("Execution did not succeed due to the following reason: Compilation error: Unknown type Person", response);
        verify(mockExecutionService).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }

    @Test
    void testValidateDRL_RuntimeException() throws DRLExecutionException {
        // Given
        when(mockExecutionService.executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        String response = agent.validateDRL("test-memory-id", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        assertEquals("Execution did not succeed due to the following reason: Unexpected error", response);
        verify(mockExecutionService).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }

    @Test
    void testValidateDRL_ExceptionWithNullMessage() throws DRLExecutionException {
        // Given
        RuntimeException exceptionWithNullMessage = new RuntimeException((String) null);
        when(mockExecutionService.executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100))
                .thenThrow(exceptionWithNullMessage);

        // When
        String response = agent.validateDRL("test-memory-id", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        assertEquals("Execution did not succeed due to the following reason: null", response);
        verify(mockExecutionService).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }

    @Test
    void testValidateDRL_WithNullInputs() throws DRLExecutionException {
        // Given
        DRLRunnerResult result = new DRLRunnerResult(Arrays.asList(), 0);
        when(mockExecutionService.executeDRLWithJsonFacts(null, null, 100))
                .thenReturn(result);

        // When
        String response = agent.validateDRL("test-memory-id", null, null);

        // Then
        assertEquals("None of the rules fired with the given test data.", response);
        verify(mockExecutionService).executeDRLWithJsonFacts(null, null, 100);
    }

    @Test
    void testValidateDRL_WithEmptyInputs() throws DRLExecutionException {
        // Given
        DRLRunnerResult result = new DRLRunnerResult(Arrays.asList(), 0);
        when(mockExecutionService.executeDRLWithJsonFacts("", "", 100))
                .thenReturn(result);

        // When
        String response = agent.validateDRL("test-memory-id", "", "");

        // Then
        assertEquals("None of the rules fired with the given test data.", response);
        verify(mockExecutionService).executeDRLWithJsonFacts("", "", 100);
    }

    @Test
    void testValidateDRL_MultipleRulesFired() throws DRLExecutionException {
        // Given
        DRLRunnerResult result = new DRLRunnerResult(
                Arrays.asList("Adult Check", "Name Validation"), 
                2
        );
        when(mockExecutionService.executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100))
                .thenReturn(result);

        // When
        String response = agent.validateDRL("test-memory-id", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        assertEquals("Code looks good", response);
        verify(mockExecutionService).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }

    @Test
    void testValidateDRL_VerifyTimeoutParameter() throws DRLExecutionException {
        // Given
        DRLRunnerResult result = new DRLRunnerResult(Arrays.asList("Adult Check"), 1);
        when(mockExecutionService.executeDRLWithJsonFacts(anyString(), anyString(), eq(100)))
                .thenReturn(result);

        // When
        agent.validateDRL("test-memory-id", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        verify(mockExecutionService).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }

    @Test
    void testValidateDRL_VerifyMemoryIdUsage() throws DRLExecutionException {
        // Given
        DRLRunnerResult result = new DRLRunnerResult(Arrays.asList("Adult Check"), 1);
        when(mockExecutionService.executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100))
                .thenReturn(result);

        // When - Test that different memory IDs don't affect execution logic
        String response1 = agent.validateDRL("memory-id-1", SAMPLE_DRL, SAMPLE_JSON);
        String response2 = agent.validateDRL("memory-id-2", SAMPLE_DRL, SAMPLE_JSON);

        // Then
        assertEquals("Code looks good", response1);
        assertEquals("Code looks good", response2);
        verify(mockExecutionService, times(2)).executeDRLWithJsonFacts(SAMPLE_DRL, SAMPLE_JSON, 100);
    }
}