package org.drools.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the MyValidation.drl validation rules.
 * Tests specific validation scenarios including fact type declarations,
 * field declarations, and helper functions.
 */
public class ValidationRulesTest {

    private final DRLVerifier verifier = new DRLVerifier();

    @Test
    @DisplayName("Valid DRL with proper declarations should pass validation")
    public void testCompletelyValidDRL() {
        String validDrl = """
            package com.example;
            
            declare Customer
                id : Long
                name : String  
                email : String
                active : boolean
            end
            
            declare Order
                orderId : String
                customer : Customer
                total : double
                date : java.util.Date
            end
            
            rule "Process Active Customer Orders"
            when
                $customer : Customer( active == true, name != null )
                $order : Order( customer == $customer, total > 100.0 )
            then
                System.out.println("Processing order: " + $order.getOrderId());
            end
            """;

        String result = verifier.verify(validDrl);
        assertEquals("Code looks good", result, "Valid DRL should pass all validation rules");
    }

    @Test
    @DisplayName("Should detect multiple undeclared fact types")
    public void testMultipleUndeclaredFactTypes() {
        String invalidDrl = """
            package com.example;
            
            rule "Multiple Undeclared Types"
            when
                $emp : Employee( department == "IT" )
                $mgr : Manager( team.size() > 5 )
                $proj : Project( status == "ACTIVE" )
            then
                System.out.println("Processing");
            end
            """;

        String result = verifier.verify(invalidDrl);
        assertTrue(result.contains("Fact type 'Employee' used in pattern but no DRL declaration"), 
                   "Should detect Employee as undeclared");
        assertTrue(result.contains("Fact type 'Manager' used in pattern but no DRL declaration"), 
                   "Should detect Manager as undeclared");  
        assertTrue(result.contains("Fact type 'Project' used in pattern but no DRL declaration"), 
                   "Should detect Project as undeclared");
    }

    @Test
    @DisplayName("Should detect undeclared fields across multiple types")
    public void testMultipleUndeclaredFields() {
        String invalidDrl = """
            package com.example;
            
            declare Person
                name : String
                age : int
            end
            
            declare Car
                make : String
                model : String
            end
            
            rule "Multiple Field Errors"
            when
                $person : Person( name != null, salary > 50000, experience > 2 )
                $car : Car( make == "Toyota", year > 2020, owner.age > 25 )
            then
                System.out.println("Processing");
            end
            """;

        String result = verifier.verify(invalidDrl);
        assertTrue(result.contains("Field 'salary' used in rule but not declared"), 
                   "Should detect undeclared salary field");
        assertTrue(result.contains("Field 'experience' used in rule but not declared"), 
                   "Should detect undeclared experience field");
        assertTrue(result.contains("Field 'year' used in rule but not declared"), 
                   "Should detect undeclared year field");
        assertTrue(result.contains("Field 'owner' used in rule but not declared"), 
                   "Should detect undeclared owner field");
    }

    @Test
    @DisplayName("Built-in Java types should not trigger validation errors")
    public void testBuiltInJavaTypes() {
        String drlWithBuiltIns = """
            package com.example;
            
            import java.util.List;
            import java.math.BigDecimal;
            
            rule "Built In Java Types"
            when
                $str : String( length() > 5, this matches ".*@.*" )
                $int : Integer( this > 0, this < 1000 )
                $long : Long( this != null )
                $double : Double( this > 0.0 )
                $bool : Boolean( this == true )
                $date : java.util.Date( time > 0 )
                $list : List( size > 0 )
                $decimal : BigDecimal( scale > 2 )
            then
                System.out.println("All built-in types");
            end
            """;

        String result = verifier.verify(drlWithBuiltIns);
        assertEquals("Code looks good", result, "Built-in types should not require DRL declarations");
    }

    @Test
    @DisplayName("Should handle complex nested field paths correctly")
    public void testComplexNestedFields() {
        String complexNestedDrl = """
            package com.example;
            
            declare Person
                name : String
                address : Address
                company : Company
            end
            
            declare Address
                street : String
                city : String
                country : Country
            end
            
            declare Country
                name : String
                code : String
            end
            
            declare Company
                name : String
                headquarters : Address
            end
            
            rule "Complex Nested Fields"
            when
                $person : Person( 
                    name != null,
                    address.city == "New York",
                    address.country.code == "US",
                    company.headquarters.city == "San Francisco"
                )
            then
                System.out.println("Complex nested access");
            end
            """;

        String result = verifier.verify(complexNestedDrl);
        assertEquals("Code looks good", result, "Properly declared nested fields should be valid");
    }

    @Test
    @DisplayName("Should detect invalid nested field paths")
    public void testInvalidNestedFieldPaths() {
        String invalidNestedDrl = """
            package com.example;
            
            declare Person
                name : String
                address : Address
            end
            
            declare Address
                street : String
                city : String
            end
            
            rule "Invalid Nested Paths"
            when
                $person : Person( 
                    name != null,
                    location.city == "NYC",         // location not declared
                    address.zipcode == "10001",     // zipcode not declared in Address
                    workplace.building.floor > 5   // workplace not declared
                )
            then
                System.out.println("Invalid nested fields");
            end
            """;

        String result = verifier.verify(invalidNestedDrl);
        assertTrue(result.contains("Field 'location' used in rule but not declared"), 
                   "Should detect undeclared location field");
        assertTrue(result.contains("Field 'workplace' used in rule but not declared"), 
                   "Should detect undeclared workplace field");
        // Note: address.zipcode should be detected as zipcode not being in Address declaration
    }

    @Test
    @DisplayName("Mixed valid and invalid patterns in same rule")
    public void testMixedValidInvalidPatterns() {
        String mixedDrl = """
            package com.example;
            
            declare Customer
                id : Long
                name : String
                email : String
            end
            
            rule "Mixed Valid Invalid"
            when
                $customer : Customer( name != null, email != null )          // Valid
                $order : Order( customerId == $customer.id )                // Order not declared
                $product : Product( price > 0, category.name == "Electronics" )  // Product not declared, category field issue
                $str : String( length() > 0 )                              // Valid built-in
            then
                System.out.println("Mixed validation");
            end
            """;

        String result = verifier.verify(mixedDrl);
        assertTrue(result.contains("Fact type 'Order' used in pattern but no DRL declaration"), 
                   "Should detect undeclared Order type");
        assertTrue(result.contains("Fact type 'Product' used in pattern but no DRL declaration"), 
                   "Should detect undeclared Product type");
        // The Customer and String patterns should be valid
    }

    @Test
    @DisplayName("Empty declare blocks should be valid")
    public void testEmptyDeclareBlocks() {
        String emptyDeclareDrl = """
            package com.example;
            
            declare EmptyEvent
            end
            
            declare AnotherEmpty
            end
            
            rule "Empty Types Usage"
            when
                $empty : EmptyEvent()
                $another : AnotherEmpty()
            then
                System.out.println("Empty types are valid");
            end
            """;

        String result = verifier.verify(emptyDeclareDrl);
        assertEquals("Code looks good", result, "Empty declare blocks should be valid");
    }

    @Test
    @DisplayName("Rule names validation with edge cases")
    public void testRuleNameValidation() {
        String ruleNameTestDrl = """
            package com.example;
            
            rule "Valid Rule Name"
            when
                $obj : Object()
            then
                System.out.println("Valid");
            end
            
            rule "anotherInvalidName" 
            when
                $obj : Object()
            then
                System.out.println("Invalid");
            end
            
            rule "123InvalidStartsWithNumber"
            when  
                $obj : Object()
            then
                System.out.println("Invalid");
            end
            
            rule "ValidName123"
            when
                $obj : Object() 
            then
                System.out.println("Valid");
            end
            """;

        String result = verifier.verify(ruleNameTestDrl);
        assertTrue(result.contains("The rule name 'anotherInvalidName' needs to start with a capital letter"), 
                   "Should detect lowercase rule name");
        assertTrue(result.contains("The rule name '123InvalidStartsWithNumber' needs to start with a capital letter"), 
                   "Should detect rule name starting with number");
        // "Valid Rule Name" and "ValidName123" should be valid
    }

    @Test  
    @DisplayName("Should handle primitive and wrapper types correctly")
    public void testPrimitiveAndWrapperTypes() {
        String primitiveWrapperDrl = """
            package com.example;
            
            declare DataRecord
                intValue : int
                integerValue : Integer
                longValue : long
                longObjectValue : Long
                doubleValue : double
                doubleObjectValue : Double
                booleanValue : boolean
                booleanObjectValue : Boolean
            end
            
            rule "Primitive And Wrapper Types"
            when
                $record : DataRecord(
                    intValue > 0,
                    integerValue != null,
                    longValue > 0L,
                    longObjectValue != null,
                    doubleValue > 0.0,
                    doubleObjectValue != null,
                    booleanValue == true,
                    booleanObjectValue == Boolean.TRUE
                )
            then
                System.out.println("Primitive and wrapper types work");
            end
            """;

        String result = verifier.verify(primitiveWrapperDrl);
        assertEquals("Code looks good", result, "Primitive and wrapper types should be handled correctly");
    }

}