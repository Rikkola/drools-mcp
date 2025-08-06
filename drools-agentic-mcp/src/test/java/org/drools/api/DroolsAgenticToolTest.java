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
    public void testGetModel_EmptyKnowledgeBase() {
        String result = droolsAgenticTool.getModel();
        
        // Should return a valid response
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Just check it doesn't crash
        System.out.println("getModel result: " + result);
    }

    @Test
    public void testRunKnowledgeBase_EmptyInput() {
        String result = droolsAgenticTool.runKnowledgeBase("{}");
        
        // Should return a valid JSON response
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("\"status\""));
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
}