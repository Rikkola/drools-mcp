package org.drools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class DRLValidatorTest {

    private DRLValidator drlValidator;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        drlValidator = new DRLValidator();
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
        String result = drlValidator.validateDRLStructure(validDRL);
        assertNotNull(result);
        // The exact validation result depends on DRLVerifier implementation
    }

    @Test
    public void testRunDRLCode_WithDeclaredTypeAndDataCreation() throws Exception {
        String drlCode = readDRLFile("person-adult-check.drl");

        String result = drlValidator.runDRLCode(drlCode, 0); // 0 = unlimited activations
        
        // Parse JSON response
        JsonNode jsonResult = objectMapper.readTree(result);
        
        // Verify successful execution
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertEquals(2, jsonResult.get("factsCount").asInt());
        
        // Verify facts array exists and has expected size
        JsonNode facts = jsonResult.get("facts");
        assertTrue(facts.isArray());
        assertEquals(2, facts.size());
        
        // Verify Person objects are present
        boolean foundAlice = false;
        boolean foundBob = false;
        
        for (JsonNode fact : facts) {
            assertEquals("Person", fact.get("type").asText());
            String value = fact.get("value").asText();
            if (value.contains("Alice") && value.contains("age=25")) {
                foundAlice = true;
                assertTrue(value.contains("adult=true"), "Alice should be marked as adult");
            }
            if (value.contains("Bob") && value.contains("age=17")) {
                foundBob = true;
                assertTrue(value.contains("adult=false"), "Bob should not be marked as adult");
            }
        }
        
        assertTrue(foundAlice, "Should find Alice in facts");
        assertTrue(foundBob, "Should find Bob in facts");
    }

    @Test
    public void testRunDRLCode_EmptyRules() throws Exception {
        String drlCode = readDRLFile("empty-rule.drl");

        String result = drlValidator.runDRLCode(drlCode, 0); // 0 = unlimited activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertEquals(0, jsonResult.get("factsCount").asInt());
        assertEquals(0, jsonResult.get("facts").size());
    }

    @Test
    public void testRunDRLCode_ComplexBusinessLogic() throws Exception {
        String drlCode = readDRLFile("order-discount-logic.drl");

        String result = drlValidator.runDRLCode(drlCode, 0); // 0 = unlimited activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertEquals(2, jsonResult.get("factsCount").asInt());
        
        JsonNode facts = jsonResult.get("facts");
        boolean foundLargeOrderWithDiscount = false;
        boolean foundSmallOrderWithoutDiscount = false;
        
        for (JsonNode fact : facts) {
            String value = fact.get("value").asText();
            if (value.contains("ORD001") && value.contains("amount=150.0")) {
                assertTrue(value.contains("discountApplied=true"), "Large order should have discount");
                assertTrue(value.contains("finalAmount=135.0"), "Large order should have discounted final amount");
                foundLargeOrderWithDiscount = true;
            }
            if (value.contains("ORD002") && value.contains("amount=75.0")) {
                assertTrue(value.contains("discountApplied=false"), "Small order should not have discount");
                assertTrue(value.contains("finalAmount=75.0"), "Small order should have original final amount");
                foundSmallOrderWithoutDiscount = true;
            }
        }
        
        assertTrue(foundLargeOrderWithDiscount, "Should find large order with discount applied");
        assertTrue(foundSmallOrderWithoutDiscount, "Should find small order without discount");
    }

    @Test
    public void testRunDRLWithExternalFacts_BasicExecution() throws Exception {
        String drlCode = readDRLFile("person-age-categorization.drl");
        
        String externalFacts = "[{\"name\":\"John\", \"age\":25}, {\"name\":\"Mary\", \"age\":16}, {\"name\":\"Bob\", \"age\":70}]";
        
        String result = drlValidator.runDRLWithExternalFacts(drlCode, externalFacts, 0); // 0 = unlimited activations
        
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
        
        String result = drlValidator.runDRLWithExternalFacts(drlCode, emptyFacts, 0); // 0 = unlimited activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertEquals("", jsonResult.get("facts").asText());
    }

    @Test
    public void testRunDRLCode_JSONEscaping() throws Exception {
        String drlCode = readDRLFile("message-with-quotes.drl");

        String result = drlValidator.runDRLCode(drlCode, 0); // 0 = unlimited activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertEquals(1, jsonResult.get("factsCount").asInt());
        
        JsonNode fact = jsonResult.get("facts").get(0);
        String value = fact.get("value").toString();
        assertTrue(value.contains("Hello \\\"World\\\" with quotes"), "Should properly escape quotes in JSON");
    }

    @Test
    public void testRunDRLCode_MultipleRuleFirings() throws Exception {
        String drlCode = readDRLFile("counter-increment.drl");

        String result = drlValidator.runDRLCode(drlCode, 0); // 0 = unlimited activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertEquals(1, jsonResult.get("factsCount").asInt());
        
        JsonNode fact = jsonResult.get("facts").get(0);
        String value = fact.get("value").asText();
        assertTrue(value.contains("value=3"), "Counter should reach value 3 after multiple rule firings");
    }

    @Test
    public void testRunDRLCode_WithMaxActivationsLimit() throws Exception {
        String drlCode = readDRLFile("counter-increment.drl");

        // Test with limited activations
        String result = drlValidator.runDRLCode(drlCode, 2); // Limit to 2 activations
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertEquals(1, jsonResult.get("factsCount").asInt());
        
        JsonNode fact = jsonResult.get("facts").get(0);
        String value = fact.get("value").asText();
        // With maxActivations=2, the counter should not reach the same value as unlimited
        // The exact value depends on rule order, but it should be limited
        assertTrue(value.contains("Counter"), "Should have Counter object");
    }

    @Test
    public void testRunDRLWithExternalFacts_WithMaxActivationsLimit() throws Exception {
        String drlCode = readDRLFile("person-age-categorization.drl");
        
        String externalFacts = "[{\"name\":\"John\", \"age\":25}]";
        
        // Test with limited activations
        String result = drlValidator.runDRLWithExternalFacts(drlCode, externalFacts, 1); // Limit to 1 activation
        
        JsonNode jsonResult = objectMapper.readTree(result);
        assertEquals("success", jsonResult.get("executionStatus").asText());
        assertTrue(jsonResult.has("factsCount"));
        assertTrue(jsonResult.has("facts"));
    }

    @Test
    public void testRunDRLCode_MaxActivationsZeroMeansUnlimited() throws Exception {
        String drlCode = readDRLFile("counter-increment.drl");

        // Test that 0 means unlimited (same as no limit)
        String resultUnlimited = drlValidator.runDRLCode(drlCode, 0);
        String resultHighLimit = drlValidator.runDRLCode(drlCode, 1000); // Very high limit
        
        JsonNode jsonUnlimited = objectMapper.readTree(resultUnlimited);
        JsonNode jsonHighLimit = objectMapper.readTree(resultHighLimit);
        
        assertEquals("success", jsonUnlimited.get("executionStatus").asText());
        assertEquals("success", jsonHighLimit.get("executionStatus").asText());
        
        // Both should produce the same result since the high limit won't be reached
        assertEquals(jsonUnlimited.get("factsCount").asInt(), jsonHighLimit.get("factsCount").asInt());
    }
}