package org.drools.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefinitionStorageTest {

    private DefinitionStorage storage;

    @BeforeEach
    void setUp() {
        storage = new DefinitionStorage();
    }

    @Test
    void testAddDefinition_Success() {
        // Given
        String name = "Person";
        String type = "declare";
        String content = "declare Person name: String age: int end";

        // When
        DefinitionStorage.DroolsDefinition result = storage.addDefinition(name, type, content);

        // Then
        assertNull(result); // No previous definition
        assertTrue(storage.hasDefinition(name));
        assertEquals(1, storage.getDefinitionCount());
    }

    @Test
    void testAddDefinition_Replace() {
        // Given
        String name = "Person";
        String type = "declare";
        String oldContent = "declare Person name: String end";
        String newContent = "declare Person name: String age: int end";

        // Add first definition
        storage.addDefinition(name, type, oldContent);

        // When - replace with new definition
        DefinitionStorage.DroolsDefinition result = storage.addDefinition(name, type, newContent);

        // Then
        assertNotNull(result); // Should return old definition
        assertEquals(oldContent, result.getContent());
        assertEquals(1, storage.getDefinitionCount()); // Still only one definition
        assertEquals(newContent, storage.getDefinition(name).getContent());
    }

    @Test
    void testAddDefinition_NullName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> storage.addDefinition(null, "declare", "content"));
        
        assertEquals("Definition name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testAddDefinition_EmptyName() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> storage.addDefinition("", "declare", "content"));
        
        assertEquals("Definition name cannot be null or empty", exception.getMessage());
    }

    @Test
    void testAddDefinition_NullType() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> storage.addDefinition("Person", null, "content"));
        
        assertEquals("Definition type cannot be null or empty", exception.getMessage());
    }

    @Test
    void testAddDefinition_NullContent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> storage.addDefinition("Person", "declare", null));
        
        assertEquals("Definition content cannot be null or empty", exception.getMessage());
    }

    @Test
    void testGetDefinition_Success() {
        // Given
        String name = "Person";
        String type = "declare";
        String content = "declare Person name: String end";
        storage.addDefinition(name, type, content);

        // When
        DefinitionStorage.DroolsDefinition result = storage.getDefinition(name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(type, result.getType());
        assertEquals(content, result.getContent());
    }

    @Test
    void testGetDefinition_NotFound() {
        // When
        DefinitionStorage.DroolsDefinition result = storage.getDefinition("NonExistent");

        // Then
        assertNull(result);
    }

    @Test
    void testGetAllDefinitions() {
        // Given
        storage.addDefinition("Person", "declare", "declare Person name: String end");
        storage.addDefinition("Order", "declare", "declare Order id: String end");

        // When
        List<DefinitionStorage.DroolsDefinition> result = storage.getAllDefinitions();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(def -> "Person".equals(def.getName())));
        assertTrue(result.stream().anyMatch(def -> "Order".equals(def.getName())));
    }

    @Test
    void testGetDefinitionsByType() {
        // Given
        storage.addDefinition("Person", "declare", "declare Person name: String end");
        storage.addDefinition("calculateAge", "function", "function int calculateAge() { return 0; }");
        storage.addDefinition("Order", "declare", "declare Order id: String end");

        // When
        List<DefinitionStorage.DroolsDefinition> declarations = storage.getDefinitionsByType("declare");
        List<DefinitionStorage.DroolsDefinition> functions = storage.getDefinitionsByType("function");

        // Then
        assertEquals(2, declarations.size());
        assertEquals(1, functions.size());
        assertTrue(declarations.stream().allMatch(def -> "declare".equals(def.getType())));
        assertTrue(functions.stream().allMatch(def -> "function".equals(def.getType())));
    }

    @Test
    void testRemoveDefinition_Success() {
        // Given
        String name = "Person";
        storage.addDefinition(name, "declare", "declare Person name: String end");

        // When
        DefinitionStorage.DroolsDefinition result = storage.removeDefinition(name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertFalse(storage.hasDefinition(name));
        assertEquals(0, storage.getDefinitionCount());
    }

    @Test
    void testRemoveDefinition_NotFound() {
        // When
        DefinitionStorage.DroolsDefinition result = storage.removeDefinition("NonExistent");

        // Then
        assertNull(result);
    }

    @Test
    void testGenerateDRLString() {
        // Given
        storage.addDefinition("DateUtils", "import", "import java.util.Date");
        storage.addDefinition("logger", "global", "global org.slf4j.Logger logger");
        storage.addDefinition("Person", "declare", "declare Person name: String age: int end");
        storage.addDefinition("calculateAge", "function", "function int calculateAge() { return 0; }");

        // When
        String result = storage.generateDRLString("org.example");

        // Then
        assertTrue(result.contains("package org.example;"));
        assertTrue(result.contains("// Imports"));
        assertTrue(result.contains("import java.util.Date"));
        assertTrue(result.contains("// Globals"));
        assertTrue(result.contains("global org.slf4j.Logger logger"));
        assertTrue(result.contains("// Declared Types"));
        assertTrue(result.contains("declare Person name: String age: int end"));
        assertTrue(result.contains("// Functions"));
        assertTrue(result.contains("function int calculateAge() { return 0; }"));
    }

    @Test
    void testGenerateDRLString_NoPackage() {
        // Given
        storage.addDefinition("Person", "declare", "declare Person name: String end");

        // When
        String result = storage.generateDRLString(null);

        // Then
        assertFalse(result.contains("package"));
        assertTrue(result.contains("declare Person name: String end"));
    }

    @Test
    void testGetSummary_Empty() {
        // When
        String result = storage.getSummary();

        // Then
        assertEquals("No definitions stored.", result);
    }

    @Test
    void testGetSummary_WithDefinitions() {
        // Given
        storage.addDefinition("Person", "declare", "declare Person name: String end");
        storage.addDefinition("Order", "declare", "declare Order id: String end");
        storage.addDefinition("calculateAge", "function", "function int calculateAge() { return 0; }");

        // When
        String result = storage.getSummary();

        // Then
        assertTrue(result.contains("Total definitions: 3"));
        assertTrue(result.contains("DECLARE (2):"));
        assertTrue(result.contains("FUNCTION (1):"));
        assertTrue(result.contains("- Person"));
        assertTrue(result.contains("- Order"));
        assertTrue(result.contains("- calculateAge"));
    }

    @Test
    void testClearAllDefinitions() {
        // Given
        storage.addDefinition("Person", "declare", "declare Person name: String end");
        storage.addDefinition("Order", "declare", "declare Order id: String end");

        // When
        storage.clearAllDefinitions();

        // Then
        assertEquals(0, storage.getDefinitionCount());
        assertTrue(storage.getAllDefinitions().isEmpty());
    }

    @Test
    void testGetDefinitionNames() {
        // Given
        storage.addDefinition("Person", "declare", "declare Person name: String end");
        storage.addDefinition("Order", "declare", "declare Order id: String end");

        // When
        List<String> names = storage.getDefinitionNames();

        // Then
        assertEquals(2, names.size());
        assertTrue(names.contains("Person"));
        assertTrue(names.contains("Order"));
    }

    @Test
    void testDroolsDefinitionSetters() {
        // Given
        DefinitionStorage.DroolsDefinition definition = new DefinitionStorage.DroolsDefinition("Person", "declare", "original content");
        long originalTime = definition.getLastModified();

        // When
        try {
            Thread.sleep(1); // Ensure some time passes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        definition.setName("UpdatedPerson");
        definition.setType("updated_declare");
        definition.setContent("updated content");

        // Then
        assertEquals("UpdatedPerson", definition.getName());
        assertEquals("updated_declare", definition.getType());
        assertEquals("updated content", definition.getContent());
        assertTrue(definition.getLastModified() >= originalTime);
    }

    @Test
    void testDroolsDefinitionToString() {
        // Given
        DefinitionStorage.DroolsDefinition definition = new DefinitionStorage.DroolsDefinition("Person", "declare", "content");

        // When
        String result = definition.toString();

        // Then
        assertTrue(result.contains("name='Person'"));
        assertTrue(result.contains("type='declare'"));
        assertTrue(result.contains("lastModified="));
    }
}