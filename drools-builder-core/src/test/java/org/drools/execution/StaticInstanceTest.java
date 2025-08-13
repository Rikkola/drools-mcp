package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Test to verify if static instances are causing conflicts
 */
public class StaticInstanceTest {

    @Test
    @DisplayName("Test multiple DRLRunner calls with same class name")
    public void testMultipleDRLRunnerCalls() {
        try {
            String drlContent = """
                declare User
                    age : int
                    adult : boolean
                end
                """;

            String jsonFacts = """
                [
                    {"_type": "User", "age": 25, "adult": false}
                ]
                """;

            System.out.println("=== First call ===");
            java.util.List<Object> result1 = DRLRunner.runDRLWithJsonFacts(drlContent, jsonFacts, 0);
            System.out.println("✅ First call succeeded: " + result1);

            System.out.println("\n=== Second call (should fail if static instance causes conflict) ===");
            java.util.List<Object> result2 = DRLRunner.runDRLWithJsonFacts(drlContent, jsonFacts, 0);
            System.out.println("✅ Second call succeeded: " + result2);

        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}