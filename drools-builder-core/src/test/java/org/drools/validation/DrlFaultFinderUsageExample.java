package org.drools.validation;

public class DrlFaultFinderUsageExample {
    
    public static void main(String[] args) {
        DrlFaultFinder finder = new DrlFaultFinder();
        
        // Example 1: DRL with a syntax error
        String faultyDrl = """
            package com.example;
            
            declare Customer
                name : String
                age : int
            end
            
            rule "adult customers"
            when
                $customer : Customer(age >= 18
            then
                System.out.println("Adult customer: " + $customer.getName());
            end
            """;
        
        DrlFaultFinder.FaultLocation fault = finder.findFaultyLine(faultyDrl);
        if (fault != null) {
            System.out.println("=== FAULT DETECTED ===");
            System.out.println("Line Number: " + fault.getLineNumber());
            System.out.println("Faulty Content: " + fault.getFaultyContent());
            System.out.println("Error Message: " + fault.getErrorMessage());
            System.out.println();
            System.out.println("Full fault details:");
            System.out.println(fault.toString());
        } else {
            System.out.println("No faults detected in the DRL!");
        }
        
        // Example 2: Valid DRL
        System.out.println("\n=== TESTING VALID DRL ===");
        String validDrl = """
            package com.example;
            
            declare Customer
                name : String
                age : int
            end
            
            rule "adult customers"
            when
                $customer : Customer(age >= 18)
            then
                System.out.println("Adult customer: " + $customer.getName());
            end
            """;
        
        DrlFaultFinder.FaultLocation validResult = finder.findFaultyLine(validDrl);
        if (validResult != null) {
            System.out.println("Unexpected fault found: " + validResult);
        } else {
            System.out.println("✅ Valid DRL - no faults detected!");
        }
    }
}