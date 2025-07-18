package org.drools.model;

import org.drools.storage.DefinitionStorage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonResponseBuilderTest {

    @Test
    void testSuccessResponse() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .build();

        // Then
        assertEquals("{\n  \"status\": \"success\"\n}", result);
    }

    @Test
    void testErrorResponse() {
        // When
        String result = JsonResponseBuilder.create()
                .error("Something went wrong")
                .build();

        // Then
        assertEquals("{\n  \"status\": \"error\",\n  \"message\": \"Something went wrong\"\n}", result);
    }

    @Test
    void testNotFoundResponse() {
        // When
        String result = JsonResponseBuilder.create()
                .notFound("Resource not found")
                .build();

        // Then
        assertEquals("{\n  \"status\": \"not_found\",\n  \"message\": \"Resource not found\"\n}", result);
    }

    @Test
    void testFieldAddition() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .field("name", "John")
                .field("age", 25)
                .build();

        // Then
        assertTrue(result.contains("\"name\": \"John\""));
        assertTrue(result.contains("\"age\": 25"));
        assertTrue(result.contains("\"status\": \"success\""));
    }

    @Test
    void testActionField() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .action("created")
                .build();

        // Then
        assertTrue(result.contains("\"action\": \"created\""));
    }

    @Test
    void testCountField() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .count(42)
                .build();

        // Then
        assertTrue(result.contains("\"count\": 42"));
    }

    @Test
    void testFactsArray() {
        // Given
        List<Object> facts = Arrays.asList("fact1", "fact2");

        // When
        String result = JsonResponseBuilder.create()
                .facts(facts)
                .build();

        // Then
        assertTrue(result.contains("\"executionStatus\": \"success\""));
        assertTrue(result.contains("\"factsCount\": 2"));
        assertTrue(result.contains("\"facts\": ["));
        assertTrue(result.contains("\"type\": \"String\""));
        assertTrue(result.contains("\"value\": \"fact1\""));
        assertTrue(result.contains("\"value\": \"fact2\""));
    }

    @Test
    void testDefinitionsArray() {
        // Given
        List<DefinitionStorage.DroolsDefinition> definitions = Arrays.asList(
            new DefinitionStorage.DroolsDefinition("Person", "declare", "declare Person name: String end"),
            new DefinitionStorage.DroolsDefinition("Order", "declare", "declare Order id: String amount: double end")
        );

        // When
        String result = JsonResponseBuilder.create()
                .success()
                .definitions(definitions)
                .build();

        // Then
        assertTrue(result.contains("\"count\": 2"));
        assertTrue(result.contains("\"definitions\": ["));
        assertTrue(result.contains("\"name\": \"Person\""));
        assertTrue(result.contains("\"name\": \"Order\""));
        assertTrue(result.contains("\"type\": \"declare\""));
    }

    @Test
    void testDrlContentField() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .drlContent("package org.example; declare Person name: String end", 1)
                .build();

        // Then
        assertTrue(result.contains("\"definitionCount\": 1"));
        assertTrue(result.contains("\"drlContent\": \"package org.example; declare Person name: String end\""));
    }

    @Test
    void testSummaryField() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .summary("Total: 5 definitions")
                .build();

        // Then
        assertTrue(result.contains("\"summary\": \"Total: 5 definitions\""));
    }

    @Test
    void testStringEscaping() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .field("message", "This contains \"quotes\" and \n newlines")
                .build();

        // Then
        assertTrue(result.contains("\"message\": \"This contains \\\"quotes\\\" and \\n newlines\""));
    }

    @Test
    void testComplexResponse() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .action("created")
                .field("name", "TestDefinition")
                .field("type", "declare")
                .count(1)
                .build();

        // Then
        assertTrue(result.contains("\"status\": \"success\""));
        assertTrue(result.contains("\"action\": \"created\""));
        assertTrue(result.contains("\"name\": \"TestDefinition\""));
        assertTrue(result.contains("\"type\": \"declare\""));
        assertTrue(result.contains("\"count\": 1"));
    }

    @Test
    void testEmptyFactsArray() {
        // Given
        List<Object> facts = Arrays.asList();

        // When
        String result = JsonResponseBuilder.create()
                .facts(facts)
                .build();

        // Then
        assertTrue(result.contains("\"executionStatus\": \"success\""));
        assertTrue(result.contains("\"factsCount\": 0"));
        assertTrue(result.contains("\"facts\": [\n  ]"));
    }

    @Test
    void testDrlContentWithNewlines() {
        // When
        String result = JsonResponseBuilder.create()
                .success()
                .drlContent("package org.example;\n\ndeclare Person\n  name: String\nend", 1)
                .build();

        // Then
        assertTrue(result.contains("\"drlContent\": \"package org.example;\\n\\ndeclare Person\\n  name: String\\nend\""));
    }

    @Test
    void testErrorMessageEscaping() {
        // When
        String result = JsonResponseBuilder.create()
                .error("Failed to parse: \"invalid syntax\"")
                .build();

        // Then
        assertTrue(result.contains("\"message\": \"Failed to parse: \\\"invalid syntax\\\"\""));
    }
}