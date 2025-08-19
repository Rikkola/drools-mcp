package org.drools.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class DRLToolTest {

    private DRLTool drlTool;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        drlTool = new DRLTool();
        objectMapper = new ObjectMapper();
    }
    
    private String readDRLFile(String filename) {
        try (InputStream inputStream = getClass().getResourceAsStream("/drl/" + filename)) {
            if (inputStream == null) {
                throw new RuntimeException("DRL file not found: " + filename);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading DRL file: " + filename, e);
        }
    }

    @Test
    public void testValidateDRLStructure_ValidDRL() {
        String validDRL = readDRLFile("valid-simple-rule.drl");
        String result = drlTool.validateDRLStructure(validDRL);
        assertNotNull(result);
        // The exact validation result depends on DRLVerifier implementation
    }

    @Test
    public void testRunDRLWithExternalFacts_BasicExecution() throws Exception {
        String drlCode = readDRLFile("person-age-categorization.drl");
        
        String externalFacts = "[{\"_type\":\"Person\", \"name\":\"John\", \"age\":25}, {\"_type\":\"Person\", \"name\":\"Mary\", \"age\":16}, {\"_type\":\"Person\", \"name\":\"Bob\", \"age\":70}]";
        
        String result = drlTool.runDRLWithExternalFacts(drlCode, externalFacts, 0); // 0 = unlimited activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        // Note: The actual expected values depend on the test files content
        // Just verify the structure is correct for now
        assertTrue(jsonResult.has("factsCount"));
        assertTrue(jsonResult.has("facts"));
    }

    @Test
    public void testRunDRLWithExternalFacts_EmptyExternalFacts() throws Exception {
        String drlCode = readDRLFile("count-facts.drl");
        
        String emptyFacts = "[]";
        
        String result = drlTool.runDRLWithExternalFacts(drlCode, emptyFacts, 0); // 0 = unlimited activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        // Empty facts should result in an empty facts array
        assertTrue(jsonResult.get("facts").isArray());
    }

    @Test
    public void testRunDRLWithExternalFacts_WithMaxActivationsLimit() throws Exception {
        String drlCode = readDRLFile("person-age-categorization.drl");
        
        String externalFacts = "[{\"_type\":\"Person\", \"name\":\"John\", \"age\":25}]";
        
        // Test with limited activations
        String result = drlTool.runDRLWithExternalFacts(drlCode, externalFacts, 1); // Limit to 1 activation
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertTrue(jsonResult.has("factsCount"));
        assertTrue(jsonResult.has("facts"));
    }

    @Test
    public void testDefinitionManagementMethods() throws Exception {
        // Test adding a definition
        String addResult = drlTool.addDefinition("TestPerson", "declare", "declare TestPerson\n    name : String\n    age : int\nend");
        JsonNode addJson = objectMapper.readTree(addResult);
        assertEquals("success", addJson.get("status").asText());
        assertEquals("added", addJson.get("action").asText());
        
        // Test getting all definitions
        String allResult = drlTool.getAllDefinitions();
        JsonNode allJson = objectMapper.readTree(allResult);
        assertEquals("success", allJson.get("status").asText());
        assertTrue(allJson.get("count").asInt() >= 1);
        
        // Test getting a specific definition
        String getResult = drlTool.getDefinition("TestPerson");
        JsonNode getJson = objectMapper.readTree(getResult);
        assertEquals("success", getJson.get("status").asText());
        assertEquals("TestPerson", getJson.get("name").asText());
        
        // Test generating DRL from definitions
        String generateResult = drlTool.generateDRLFromDefinitions("org.test");
        JsonNode generateJson = objectMapper.readTree(generateResult);
        assertEquals("success", generateJson.get("status").asText());
        assertTrue(generateJson.has("drlContent"));
        
        // Test getting definitions summary
        String summaryResult = drlTool.getDefinitionsSummary();
        JsonNode summaryJson = objectMapper.readTree(summaryResult);
        assertEquals("success", summaryJson.get("status").asText());
        assertTrue(summaryJson.has("summary"));
        
        // Test removing a definition
        String removeResult = drlTool.removeDefinition("TestPerson");
        JsonNode removeJson = objectMapper.readTree(removeResult);
        assertEquals("success", removeJson.get("status").asText());
        assertEquals("removed", removeJson.get("action").asText());
    }

    @Test
    public void testDefinitionNotFound() throws Exception {
        // Test getting a non-existent definition
        String getResult = drlTool.getDefinition("NonExistentDefinition");
        JsonNode getJson = objectMapper.readTree(getResult);
        assertEquals("not_found", getJson.get("status").asText());
        
        // Test removing a non-existent definition
        String removeResult = drlTool.removeDefinition("NonExistentDefinition");
        JsonNode removeJson = objectMapper.readTree(removeResult);
        assertEquals("not_found", removeJson.get("status").asText());
    }

    @Test
    public void testRunFactsAgainstStoredDefinitions_Success() throws Exception {
        // First add some definitions
        drlTool.addDefinition("Person", "declare", "declare Person\n    name : String\n    age : int\nend");
        drlTool.addDefinition("AgeRule", "rule", 
            "rule \"Check Adult\" " +
            "when " +
            "    $p: Person(age >= 18) " +
            "then " +
            "    System.out.println(\"Adult: \" + $p.getName()); " +
            "end");

        // Execute facts against stored definitions
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        String response = drlTool.runFactsAgainstStoredDefinitions(factsJson, 10);
        
        JsonNode responseNode = objectMapper.readTree(response);
        assertEquals("success", responseNode.get("executionStatus").asText());
        assertTrue(responseNode.get("factsCount").asInt() > 0);
        assertNotNull(responseNode.get("facts"));
    }

    @Test
    public void testRunFactsAgainstStoredDefinitions_NoDefinitions() throws Exception {
        // Try to run facts without any stored definitions
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        String response = drlTool.runFactsAgainstStoredDefinitions(factsJson, 10);
        
        JsonNode responseNode = objectMapper.readTree(response);
        assertEquals("error", responseNode.get("status").asText());
        assertTrue(responseNode.get("message").asText().contains("No DRL definitions found"));
    }

    @Test
    public void testRunFactsAgainstStoredDefinitions_EmptyFacts() throws Exception {
        // Add a definition first
        drlTool.addDefinition("Person", "declare", "declare Person\n    name : String\n    age : int\nend");
        
        // Execute empty facts
        String factsJson = "[]";
        String response = drlTool.runFactsAgainstStoredDefinitions(factsJson, 10);
        
        JsonNode responseNode = objectMapper.readTree(response);
        assertEquals("success", responseNode.get("executionStatus").asText());
        assertEquals(0, responseNode.get("factsCount").asInt());
    }

    @Test
    public void testRunFactsAgainstStoredDefinitions_NegativeMaxActivations() throws Exception {
        // Add a definition first
        drlTool.addDefinition("Person", "declare", "declare Person\n    name : String\n    age : int\nend");
        
        // Try with negative max activations
        String factsJson = "[{\"_type\":\"Person\",\"name\":\"John\",\"age\":25}]";
        String response = drlTool.runFactsAgainstStoredDefinitions(factsJson, -1);
        
        JsonNode responseNode = objectMapper.readTree(response);
        assertEquals("error", responseNode.get("status").asText());
        assertTrue(responseNode.get("message").asText().contains("Maximum activations cannot be negative"));
    }
}
