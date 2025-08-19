package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Isolated test to reproduce the "No class definition found for type" issue
 * with declared types in DRL and JSON facts.
 */
public class DeclaredTypeTest {

    @Test
    @DisplayName("Should successfully execute DRL with declared User type and JSON facts")
    public void testDeclaredTypeWithJsonFacts() {
        // DRL content with a declared User type (similar to what agents generate)
        String drlContent = """
            package com.example;
            
            declare User
                age : int
                adult : boolean
            end
            
            rule "Classify Adult"
            when
                $user : User(age >= 18)
            then
                modify($user) { setAdult(true) }
                System.out.println("User is an adult: " + $user.getAge());
            end
            
            rule "Classify Minor"
            when
                $user : User(age < 18)
            then
                modify($user) { setAdult(false) }
                System.out.println("User is a minor: " + $user.getAge());
            end
            """;

        // JSON facts that should map to the declared User type
        String jsonFacts = """
            [
                {"_type": "User", "age": 25, "adult": false},
                {"_type": "User", "age": 16, "adult": false}
            ]
            """;

        // This should work with our fix, but let's verify
        try {
            DRLRunnerResult result = DRLRunner.runDRLWithJsonFacts(drlContent, jsonFacts, 0);
            
            // Verify that facts were processed
            assertNotNull(result, "Result should not be null");
            assertEquals(2, result.objects().size(), "Should have 2 facts in working memory");
            
            System.out.println("SUCCESS: DRL executed with declared types");
            result.objects().forEach(fact -> System.out.println("Fact: " + fact));
            
        } catch (Exception e) {
            System.err.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            fail("DRL execution failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should debug the type registration process")
    public void testTypeRegistrationDebugging() {
        String drlContent = """
            declare TestUser
                name : String
                age : int
            end
            
            rule "Test Rule"
            when
                $user : TestUser(age > 0)
            then
                System.out.println("Found user: " + $user.getName());
            end
            """;

        String jsonFacts = """
            [{"_type": "TestUser", "name": "John", "age": 25}]
            """;

        try {
            System.out.println("=== DEBUGGING TYPE REGISTRATION ===");
            System.out.println("DRL Content:");
            System.out.println(drlContent);
            System.out.println("\nJSON Facts:");
            System.out.println(jsonFacts);
            
            DRLRunnerResult result = DRLRunner.runDRLWithJsonFacts(drlContent, jsonFacts, 0);
            
            System.out.println("\nRESULT:");
            System.out.println("Facts count: " + result.objects().size());
            result.objects().forEach(fact -> System.out.println("  " + fact));
            
        } catch (Exception e) {
            System.err.println("DEBUGGING FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}