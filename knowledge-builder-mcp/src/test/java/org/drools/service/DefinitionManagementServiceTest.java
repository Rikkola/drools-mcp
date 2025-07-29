package org.drools.service;

import org.drools.exception.DefinitionNotFoundException;
import org.drools.storage.DefinitionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefinitionManagementServiceTest {

    @Mock
    private DefinitionStorage mockStorage;
    
    private DefinitionManagementService definitionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        definitionService = new DefinitionManagementService(mockStorage);
    }

    @Test
    void testAddDefinition_Success() {
        // Given
        String name = "Person";
        String type = "declare";
        String content = "declare Person name: String age: int end";
        DefinitionStorage.DroolsDefinition oldDefinition = new DefinitionStorage.DroolsDefinition("Person", "declare", "old content");
        
        when(mockStorage.addDefinition(name, type, content)).thenReturn(oldDefinition);

        // When
        DefinitionStorage.DroolsDefinition result = definitionService.addDefinition(name, type, content);

        // Then
        assertEquals(oldDefinition, result);
        verify(mockStorage).addDefinition(name, type, content);
    }

    @Test
    void testAddDefinition_NullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> definitionService.addDefinition(null, "declare", "content"));
        
        assertEquals("Definition name cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockStorage);
    }

    @Test
    void testAddDefinition_EmptyName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> definitionService.addDefinition("", "declare", "content"));
        
        assertEquals("Definition name cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockStorage);
    }

    @Test
    void testAddDefinition_NullType() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> definitionService.addDefinition("Person", null, "content"));
        
        assertEquals("Definition type cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockStorage);
    }

    @Test
    void testAddDefinition_NullContent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> definitionService.addDefinition("Person", "declare", null));
        
        assertEquals("Definition content cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockStorage);
    }

    @Test
    void testGetDefinition_Success() {
        // Given
        String name = "Person";
        DefinitionStorage.DroolsDefinition definition = new DefinitionStorage.DroolsDefinition(name, "declare", "content");
        when(mockStorage.getDefinition(name)).thenReturn(definition);

        // When
        DefinitionStorage.DroolsDefinition result = definitionService.getDefinition(name);

        // Then
        assertEquals(definition, result);
        verify(mockStorage).getDefinition(name);
    }

    @Test
    void testGetDefinition_NotFound() {
        // Given
        String name = "NonExistent";
        when(mockStorage.getDefinition(name)).thenReturn(null);

        // When & Then
        DefinitionNotFoundException exception = assertThrows(DefinitionNotFoundException.class, 
            () -> definitionService.getDefinition(name));
        
        assertEquals("Definition with name 'NonExistent' not found", exception.getMessage());
        assertEquals(name, exception.getDefinitionName());
        verify(mockStorage).getDefinition(name);
    }

    @Test
    void testGetDefinition_NullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> definitionService.getDefinition(null));
        
        assertEquals("Definition name cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockStorage);
    }

    @Test
    void testGetAllDefinitions() {
        // Given
        List<DefinitionStorage.DroolsDefinition> definitions = Arrays.asList(
            new DefinitionStorage.DroolsDefinition("Person", "declare", "content1"),
            new DefinitionStorage.DroolsDefinition("Order", "declare", "content2")
        );
        when(mockStorage.getAllDefinitions()).thenReturn(definitions);

        // When
        List<DefinitionStorage.DroolsDefinition> result = definitionService.getAllDefinitions();

        // Then
        assertEquals(definitions, result);
        verify(mockStorage).getAllDefinitions();
    }

    @Test
    void testRemoveDefinition_Success() {
        // Given
        String name = "Person";
        DefinitionStorage.DroolsDefinition definition = new DefinitionStorage.DroolsDefinition(name, "declare", "content");
        when(mockStorage.removeDefinition(name)).thenReturn(definition);

        // When
        DefinitionStorage.DroolsDefinition result = definitionService.removeDefinition(name);

        // Then
        assertEquals(definition, result);
        verify(mockStorage).removeDefinition(name);
    }

    @Test
    void testRemoveDefinition_NotFound() {
        // Given
        String name = "NonExistent";
        when(mockStorage.removeDefinition(name)).thenReturn(null);

        // When & Then
        DefinitionNotFoundException exception = assertThrows(DefinitionNotFoundException.class, 
            () -> definitionService.removeDefinition(name));
        
        assertEquals("Definition with name 'NonExistent' not found", exception.getMessage());
        assertEquals(name, exception.getDefinitionName());
        verify(mockStorage).removeDefinition(name);
    }

    @Test
    void testRemoveDefinition_NullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> definitionService.removeDefinition(null));
        
        assertEquals("Definition name cannot be null or empty", exception.getMessage());
        verifyNoInteractions(mockStorage);
    }

    @Test
    void testGenerateDRLFromDefinitions() {
        // Given
        String packageName = "org.example";
        String expectedDRL = "package org.example; declare Person name: String end";
        when(mockStorage.generateDRLString(packageName)).thenReturn(expectedDRL);

        // When
        String result = definitionService.generateDRLFromDefinitions(packageName);

        // Then
        assertEquals(expectedDRL, result);
        verify(mockStorage).generateDRLString(packageName);
    }

    @Test
    void testGetDefinitionCount() {
        // Given
        int expectedCount = 5;
        when(mockStorage.getDefinitionCount()).thenReturn(expectedCount);

        // When
        int result = definitionService.getDefinitionCount();

        // Then
        assertEquals(expectedCount, result);
        verify(mockStorage).getDefinitionCount();
    }

    @Test
    void testGetDefinitionsSummary() {
        // Given
        String expectedSummary = "Summary of definitions...";
        when(mockStorage.getSummary()).thenReturn(expectedSummary);

        // When
        String result = definitionService.getDefinitionsSummary();

        // Then
        assertEquals(expectedSummary, result);
        verify(mockStorage).getSummary();
    }

    @Test
    void testDefaultConstructor() {
        // Given - use default constructor
        DefinitionManagementService defaultService = new DefinitionManagementService();

        // When - should not throw exception
        List<DefinitionStorage.DroolsDefinition> result = defaultService.getAllDefinitions();

        // Then - should return empty list (depends on actual DefinitionStorage implementation)
        assertNotNull(result);
    }
}