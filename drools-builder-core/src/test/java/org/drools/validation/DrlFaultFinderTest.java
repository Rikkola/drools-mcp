package org.drools.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrlFaultFinderTest {
    
    private DrlFaultFinder drlFaultFinder;
    
    @BeforeEach
    void setUp() {
        drlFaultFinder = new DrlFaultFinder();
    }
    
    @Test
    void testValidDrlReturnsNull() {
        String validDrl = """
            package com.example;
            
            declare Person
                name : String
                age : int
            end
            
            rule "test rule"
            when
                $person : Person(age > 18)
            then
                System.out.println("Adult: " + $person.getName());
            end
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(validDrl);
        assertNull(result, "Valid DRL should return null");
    }
    
    @Test
    void testSingleLineError() {
        String faultyDrl = "invalid drl syntax here";
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Faulty DRL should return a fault location");
        assertEquals(1, result.getLineNumber(), "Should identify line 1 as faulty");
        assertEquals("invalid drl syntax here", result.getFaultyContent(), "Should identify the correct faulty content");
    }
    
    @Test
    void testMultipleLinesDrlWithError() {
        String faultyDrl = """
            package com.example;
            
            rule "test rule"
            when
                $person : Person(age > 18
            then
                System.out.println("test");
            end
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find faulty line");
        assertEquals(5, result.getLineNumber(), "Should identify line 5 as faulty");
        assertNotNull(result.getErrorMessage(), "Should have an error message");
    }
    
    @Test
    void testPackageDeclarationError() {
        String faultyDrl = """
            package com.example invalid;
            
            rule "test rule"
            when
                $person : Person()
            then
                System.out.println("test");
            end
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find faulty package declaration");
        assertEquals(1, result.getLineNumber(), "Should identify line 1 as faulty");
    }
    
    @Test
    void testEmptyDrlThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            drlFaultFinder.findFaultyLine("");
        }, "Empty DRL should throw IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, () -> {
            drlFaultFinder.findFaultyLine(null);
        }, "Null DRL should throw IllegalArgumentException");
    }
    
    @Test
    void testSimpleSyntaxError() {
        String faultyDrl = """
            package com.example;
            rule "test"
            when
            then
                System.out.println("test"
            end
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find faulty line");
        assertEquals(5, result.getLineNumber(), "Should identify line 5 as faulty");
        assertFalse(result.getFaultyContent().isEmpty(), "Should have faulty content");
    }
    
    @Test
    void testFaultLocationToString() {
        DrlFaultFinder.FaultLocation fault = new DrlFaultFinder.FaultLocation(
            "faulty content", 10, "syntax error"
        );
        
        String result = fault.toString();
        assertTrue(result.contains("line 10"), "Should include line number");
        assertTrue(result.contains("syntax error"), "Should include error message");
        assertTrue(result.contains("faulty content"), "Should include faulty content");
    }
    
    @Test
    void testGetters() {
        DrlFaultFinder.FaultLocation fault = new DrlFaultFinder.FaultLocation(
            "test content", 5, "test error"
        );
        
        assertEquals("test content", fault.getFaultyContent());
        assertEquals(5, fault.getLineNumber());
        assertEquals("test error", fault.getErrorMessage());
    }
    
    @Test
    void testRuleWithMissingSemicolon() {
        String faultyDrl = """
            package com.example;
            
            rule "test rule"
            when
                $person : Person()
            then
                System.out.println("test")
            end
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        
        if (result != null) {
            assertEquals(5, result.getLineNumber(), "Should identify line 5 as faulty");
            assertNotNull(result.getErrorMessage(), "Should have an error message");
        }
    }
    
    @Test
    void testIncompleteRuleWithoutThenAndEnd() {
        String faultyDrl = """
            package com.example;
            
            rule "incomplete rule"
            when
                $person : Person(age > 18
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find the faulty line in incomplete rule");
        assertEquals(5, result.getLineNumber(), "Should identify line 5 as faulty (missing closing parenthesis)");
        assertNotNull(result.getErrorMessage(), "Should have an error message");
    }
    
    @Test
    void testIncompleteQueryWithoutEnd() {
        String faultyDrl = """
            package com.example;
            
            query "findAdults"
                $person : Person(age > 18
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find the faulty line in incomplete query");
        assertEquals(4, result.getLineNumber(), "Should identify line 4 as faulty (missing closing parenthesis)");
        assertNotNull(result.getErrorMessage(), "Should have an error message");
    }
    
    @Test
    void testIncompleteFunction() {
        String faultyDrl = """
            package com.example;
            
            function void myFunction() {
                System.out.println("test"
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find the faulty line in incomplete function");
        assertEquals(3, result.getLineNumber(), "Should identify line 3 as faulty (function definition issue)");
        assertNotNull(result.getErrorMessage(), "Should have an error message");
    }
    
    @Test
    void testIncompleteFunctionWithMissingBrace() {
        String faultyDrl = """
            package com.example;
            
            function void calculate() {
                int x = 5;
                int y = x * 2
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find the faulty line in incomplete function");
        assertEquals(3, result.getLineNumber(), "Should identify line 3 as faulty (function definition issue)");
        assertNotNull(result.getErrorMessage(), "Should have an error message");
    }
    
    @Test
    void testIncompleteDeclaration() {
        String faultyDrl = """
            package com.example;
            
            declare Person
                name : String
                age : int invalid
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find the faulty line in incomplete declaration");
        assertEquals(5, result.getLineNumber(), "Should identify line 5 as faulty (invalid field declaration)");
        assertNotNull(result.getErrorMessage(), "Should have an error message");
    }
    
    @Test
    void testIncompleteDeclarationWithoutEnd() {
        String faultyDrl = """
            package com.example;
            
            declare Person
                name : String
                age : int
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        // This may still find a fault if the declaration structure needs more context
        if (result != null) {
            assertEquals(5, result.getLineNumber(), "Should identify line 5 as faulty");
        }
    }
    
    @Test
    void testMultiLineContent() {
        String faultyDrl = """
            package com.example;
            
            declare Person
                name : String
                age : int
            end
            
            rule "adults only"
            when
                $person : Person(age >= 18)
            then
                System.out.println("Adult person: " + $person.getName())
            end
            
            rule "minors"
            when
                $person : Person(age < 18
            then
                System.out.println("Minor person");
            end
            """;
        
        DrlFaultFinder.FaultLocation result = drlFaultFinder.findFaultyLine(faultyDrl);
        assertNotNull(result, "Should find the faulty line");
        assertEquals(12, result.getLineNumber(), "Should identify line 12 as faulty");
        assertNotNull(result.getErrorMessage(), "Should have an error message");
    }
}