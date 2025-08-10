package org.drools.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DRLVerifierTest {

    @Test
    public void testValidDrl() {
        DRLVerifier verifier = new DRLVerifier();
        String validDrlContent = "package org.drools;\n" +
                                "rule \"Example Rule\"\n" +
                                "when\n" +
                                "    $fact : Object()\n" +
                                "then\n" +
                                "    System.out.println(\"This is an example rule\");\n" +
                                "end";

        String result = verifier.verify(validDrlContent);
        assertEquals("Code looks good", result);
    }

    @Test
    public void testInvalidDrl() {
        DRLVerifier verifier = new DRLVerifier();

        String invalidDrlContent =
                "package org.drools;\n" +
                "rule \"example Rule\"\n" +
                "when\n" +
                "    $fact : Object()\n" +
                "then\n" +
                "    System.out.println(\"This is an example rule\");\n" +
                "end";

        String result = verifier.verify(invalidDrlContent);
        assertTrue(result.contains("The rule name 'example Rule' needs to start with a capital letter."), "Result should contain error information");
    }

    @Test
    public void testValidDrlWithDeclaredTypes() {
        DRLVerifier verifier = new DRLVerifier();
        String validDrlWithDeclare = 
                "package com.example.test;\n" +
                "\n" +
                "declare Person\n" +
                "    name : String\n" +
                "    age : int\n" +
                "    salary : double\n" +
                "end\n" +
                "\n" +
                "rule \"Valid Person Rule\"\n" +
                "when\n" +
                "    $p : Person( name != null, age > 18 )\n" +
                "then\n" +
                "    System.out.println(\"Valid person: \" + $p.getName());\n" +
                "end";

        String result = verifier.verify(validDrlWithDeclare);
        assertEquals("Code looks good", result);
    }

    @Test
    public void testUndeclaredFactType() {
        DRLVerifier verifier = new DRLVerifier();
        String drlWithUndeclaredType = 
                "package com.example.test;\n" +
                "\n" +
                "rule \"Invalid Employee Rule\"\n" +
                "when\n" +
                "    $e : Employee( department == \"IT\" )\n" +
                "then\n" +
                "    System.out.println(\"Employee found\");\n" +
                "end";

        String result = verifier.verify(drlWithUndeclaredType);
        assertTrue(result.contains("Fact type 'Employee' used in pattern but no DRL declaration"), 
                   "Should detect undeclared fact type");
    }

    @Test 
    public void testUndeclaredField() {
        DRLVerifier verifier = new DRLVerifier();
        String drlWithUndeclaredField = 
                "package com.example.test;\n" +
                "\n" +
                "declare Person\n" +
                "    name : String\n" +
                "    age : int\n" +
                "end\n" +
                "\n" +
                "rule \"Invalid Person Field Rule\"\n" +
                "when\n" +
                "    $p : Person( name != null, experience > 5 )\n" +
                "then\n" +
                "    System.out.println(\"Experienced person\");\n" +
                "end";

        String result = verifier.verify(drlWithUndeclaredField);
        assertTrue(result.contains("Field 'experience' used in rule but not declared"), 
                   "Should detect undeclared field");
    }

    @Test
    public void testBuiltInTypesAreValid() {
        DRLVerifier verifier = new DRLVerifier();
        String drlWithBuiltInTypes = 
                "package com.example.test;\n" +
                "\n" +
                "rule \"Built In Types Rule\"\n" +
                "when\n" +
                "    $str : String( length > 0 )\n" +
                "    $int : Integer( this > 10 )\n" +
                "then\n" +
                "    System.out.println(\"Built-in types work\");\n" +
                "end";

        String result = verifier.verify(drlWithBuiltInTypes);
        assertEquals("Code looks good", result);
    }

    @Test
    public void testMultipleValidationErrors() {
        DRLVerifier verifier = new DRLVerifier();
        String drlWithMultipleErrors = 
                "package com.example.test;\n" +
                "\n" +
                "declare Vehicle\n" +
                "    make : String\n" +
                "    model : String\n" +
                "end\n" +
                "\n" +
                "rule \"lowercase rule name\"\n" +  // Rule name error
                "when\n" +
                "    $e : Employee( department == \"IT\" )\n" +  // Undeclared type
                "    $v : Vehicle( make == \"Toyota\", year > 2020 )\n" +  // Undeclared field
                "then\n" +
                "    System.out.println(\"Multiple errors\");\n" +
                "end";

        String result = verifier.verify(drlWithMultipleErrors);
        
        // Should contain all three types of validation errors
        assertTrue(result.contains("needs to start with a capital letter"), 
                   "Should detect rule name error");
        assertTrue(result.contains("Fact type 'Employee' used in pattern but no DRL declaration"), 
                   "Should detect undeclared fact type");
        assertTrue(result.contains("Field 'year' used in rule but not declared"), 
                   "Should detect undeclared field");
    }

    @Test
    public void testNestedFieldPaths() {
        DRLVerifier verifier = new DRLVerifier();
        String drlWithNestedFields = 
                "package com.example.test;\n" +
                "\n" +
                "declare Person\n" +
                "    name : String\n" +
                "    address : Address\n" +
                "end\n" +
                "\n" +
                "declare Address\n" +
                "    city : String\n" +
                "    street : String\n" +
                "end\n" +
                "\n" +
                "rule \"Nested Field Rule\"\n" +
                "when\n" +
                "    $p : Person( name != null, address.city == \"New York\" )\n" +
                "then\n" +
                "    System.out.println(\"Person from NYC\");\n" +
                "end";

        String result = verifier.verify(drlWithNestedFields);
        assertEquals("Code looks good", result);
    }

    @Test
    public void testInvalidNestedField() {
        DRLVerifier verifier = new DRLVerifier();
        String drlWithInvalidNestedField = 
                "package com.example.test;\n" +
                "\n" +
                "declare Person\n" +
                "    name : String\n" +
                "    address : Address\n" +
                "end\n" +
                "\n" +
                "declare Address\n" +
                "    city : String\n" +
                "    street : String\n" +
                "end\n" +
                "\n" +
                "rule \"Invalid Nested Field Rule\"\n" +
                "when\n" +
                "    $p : Person( name != null, location.city == \"New York\" )\n" +  // location not declared
                "then\n" +
                "    System.out.println(\"Invalid nested field\");\n" +
                "end";

        String result = verifier.verify(drlWithInvalidNestedField);
        assertTrue(result.contains("Field 'location' used in rule but not declared"), 
                   "Should detect undeclared nested field base");
    }

    @Test
    public void testEmptyDeclareBlock() {
        DRLVerifier verifier = new DRLVerifier();
        String drlWithEmptyDeclare = 
                "package com.example.test;\n" +
                "\n" +
                "declare EmptyType\n" +
                "end\n" +
                "\n" +
                "rule \"Empty Type Rule\"\n" +
                "when\n" +
                "    $e : EmptyType()\n" +
                "then\n" +
                "    System.out.println(\"Empty type usage\");\n" +
                "end";

        String result = verifier.verify(drlWithEmptyDeclare);
        // This should be valid - empty declare blocks are allowed
        assertEquals("Code looks good", result);
    }

}
