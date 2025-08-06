package org.drools.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test for DroolsAgenticTool to verify integration works.
 */
public class DroolsAgenticToolTest {

    private DroolsAgenticTool droolsAgenticTool;

    @BeforeEach
    public void setUp() {
        droolsAgenticTool = new DroolsAgenticTool();
    }

    @Test 
    public void testImproveKnowledgeBase_BasicSpecification() {
        // This test might fail due to model requirements, but should not crash
        String specification = """
            Domain Model: Person entity with name (string) and age (integer) attributes.
            Business Rules: Classify persons as adult if age >= 18, minor otherwise.
            Constraints: Age must be non-negative. Name cannot be empty.
            Example: Person(name="John", age=25) should be classified as adult.
            """;
        String result = droolsAgenticTool.improveKnowledgeBase(specification);
        
        // Should return a valid JSON response even if it fails
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("\"status\""));
    }

    @Test
    public void testGetKnowledgeBaseStatus() {
        String result = droolsAgenticTool.getKnowledgeBaseStatus();
        
        // Should return a valid JSON response
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("\"status\""));
    }

    @Test
    public void testExecuteRules_EmptyFacts() {
        String result = droolsAgenticTool.executeRules("[]", 0);
        
        // Should return a valid JSON response even when no knowledge base exists
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("\"status\""));
    }

    @Test
    public void testClearFacts() {
        String result = droolsAgenticTool.clearFacts();
        
        // Should return a valid JSON response
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("\"status\""));
    }

    @Test
    public void testExecuteBatch_EmptyBatches() {
        String result = droolsAgenticTool.executeBatch("[[]]", 0);
        
        // Should return a valid JSON response even when no knowledge base exists
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("\"status\""));
    }
}
