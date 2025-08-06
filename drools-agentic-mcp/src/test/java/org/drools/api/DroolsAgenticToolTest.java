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
    public void testImproveKnowledgeBase_BasicRequirements() {
        // This test might fail due to model requirements, but should not crash
        String result = droolsAgenticTool.improveKnowledgeBase("Create a simple Person rule");
        
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
