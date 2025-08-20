package org.drools.execution;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DRLPopulatorRunnerTest {

    @Test
    public void testRunDRLWithJsonPopulation() {
        String drlContent = 
            "package org.drools.person;\n" +
            "declare Person\n" +
            "    name : String\n" +
            "    age : int\n" +
            "    adult : boolean = false\n" +
            "end\n" +
            "rule 'Mark as Adult'\n" +
            "when\n" +
            "    $p : Person(age >= 18, adult == false)\n" +
            "then\n" +
            "    modify($p) { setAdult(true); }\n" +
            "    System.out.println('Marked ' + $p.getName() + ' as adult');\n" +
            "end";
        
        String jsonData = "{\"_type\":\"Person\", \"name\":\"John\", \"age\":25}";
        
        DRLRunnerResult result = DRLPopulatorRunner.runDRL(drlContent, jsonData);
        List<Object> facts = result.objects();
        
        assertNotNull(facts, "Facts should not be null");
        assertEquals(1, facts.size(), "Should have exactly 1 fact");
        
        // Verify the person was created and marked as adult
        Object person = facts.get(0);
        String personString = person.toString();
        assertTrue(personString.contains("John"), "Person should have name John");
        assertTrue(personString.contains("age=25"), "Person should have age 25");
        assertTrue(personString.contains("adult=true"), "Person should be marked as adult");
    }

    @Test
    public void testRunDRLWithJsonPopulationMinor() {
        String drlContent = 
            "package org.drools.person;\n" +
            "declare Person\n" +
            "    name : String\n" +
            "    age : int\n" +
            "    adult : boolean = false\n" +
            "end\n" +
            "rule 'Mark as Adult'\n" +
            "when\n" +
            "    $p : Person(age >= 18, adult == false)\n" +
            "then\n" +
            "    modify($p) { setAdult(true); }\n" +
            "    System.out.println('Marked ' + $p.getName() + ' as adult');\n" +
            "end";
        
        String jsonData = "{\"_type\":\"Person\", \"name\":\"Jane\", \"age\":16}";
        
        DRLRunnerResult result = DRLPopulatorRunner.runDRL(drlContent, jsonData);
        List<Object> facts = result.objects();
        
        assertNotNull(facts, "Facts should not be null");
        assertEquals(1, facts.size(), "Should have exactly 1 fact");
        
        // Verify the person was created but not marked as adult
        Object person = facts.get(0);
        String personString = person.toString();
        assertTrue(personString.contains("Jane"), "Person should have name Jane");
        assertTrue(personString.contains("age=16"), "Person should have age 16");
        assertTrue(personString.contains("adult=false"), "Person should not be marked as adult");
    }

    @Test
    public void testRunDRLWithMaxRuns() {
        String drlContent = 
            "package org.drools.person;\n" +
            "declare Person\n" +
            "    name : String\n" +
            "    age : int\n" +
            "    adult : boolean = false\n" +
            "end\n" +
            "rule 'Mark as Adult'\n" +
            "when\n" +
            "    $p : Person(age >= 18, adult == false)\n" +
            "then\n" +
            "    modify($p) { setAdult(true); }\n" +
            "    System.out.println('Marked ' + $p.getName() + ' as adult');\n" +
            "end";
        
        String jsonData = "{\"_type\":\"Person\", \"name\":\"Bob\", \"age\":25}";
        
        // Test with maxRuns limit
        DRLRunnerResult result = DRLPopulatorRunner.runDRL(drlContent, jsonData, 1);
        List<Object> facts = result.objects();
        
        assertNotNull(facts, "Facts should not be null");
        assertEquals(1, facts.size(), "Should have exactly 1 fact");
        
        // Test with unlimited runs (0)
        DRLRunnerResult unlimitedResult = DRLPopulatorRunner.runDRL(drlContent, jsonData, 0);
        List<Object> unlimitedFacts = unlimitedResult.objects();
        
        assertNotNull(unlimitedFacts, "Unlimited facts should not be null");
        assertEquals(1, unlimitedFacts.size(), "Should have exactly 1 fact");
    }

    @Test
    public void testRunDRLWithExplicitPackageAndType() {
        String drlContent = 
            "package org.drools.test;\n" +
            "declare Employee\n" +
            "    empId : String\n" +
            "    salary : double\n" +
            "    bonus : double = 0.0\n" +
            "end\n" +
            "rule 'Calculate Bonus'\n" +
            "when\n" +
            "    $e : Employee(salary > 50000, bonus == 0.0)\n" +
            "then\n" +
            "    modify($e) { setBonus($e.getSalary() * 0.1); }\n" +
            "    System.out.println('Calculated bonus for ' + $e.getEmpId());\n" +
            "end";
        
        String jsonData = "{\"_type\":\"Employee\", \"empId\":\"EMP001\", \"salary\":60000.0}";
        
        DRLRunnerResult result = DRLPopulatorRunner.runDRL(drlContent, jsonData, "org.drools.test", "Employee");
        List<Object> facts = result.objects();
        
        assertNotNull(facts, "Facts should not be null");
        assertEquals(1, facts.size(), "Should have exactly 1 fact");
        
        Object employee = facts.get(0);
        String employeeString = employee.toString();
        assertTrue(employeeString.contains("EMP001"), "Employee should have ID EMP001");
        assertTrue(employeeString.contains("salary=60000"), "Employee should have salary 60000");
        assertTrue(employeeString.contains("bonus=6000"), "Employee should have bonus calculated");
    }

    @Test
    public void testRunDRLWithExplicitPackageAndTypeWithMaxRuns() {
        String drlContent = 
            "package org.drools.test;\n" +
            "declare Employee\n" +
            "    empId : String\n" +
            "    salary : double\n" +
            "    bonus : double = 0.0\n" +
            "end\n" +
            "rule 'Calculate Bonus'\n" +
            "when\n" +
            "    $e : Employee(salary > 50000, bonus == 0.0)\n" +
            "then\n" +
            "    modify($e) { setBonus($e.getSalary() * 0.1); }\n" +
            "    System.out.println('Calculated bonus for ' + $e.getEmpId());\n" +
            "end";
        
        String jsonData = "{\"_type\":\"Employee\", \"empId\":\"EMP002\", \"salary\":70000.0}";
        
        DRLRunnerResult result = DRLPopulatorRunner.runDRL(drlContent, jsonData, "org.drools.test", "Employee", 2);
        List<Object> facts = result.objects();
        
        assertNotNull(facts, "Facts should not be null");
        assertEquals(1, facts.size(), "Should have exactly 1 fact");
        
        Object employee = facts.get(0);
        String employeeString = employee.toString();
        assertTrue(employeeString.contains("EMP002"), "Employee should have ID EMP002");
        assertTrue(employeeString.contains("salary=70000"), "Employee should have salary 70000");
        assertTrue(employeeString.contains("bonus=7000"), "Employee should have bonus calculated");
    }

    @Test
    public void testInvalidJsonHandling() {
        String drlContent = 
            "package org.drools.person;\n" +
            "declare Person\n" +
            "    name : String\n" +
            "    age : int\n" +
            "end";
        
        String invalidJson = "{invalid json}";
        
        assertThrows(RuntimeException.class, () -> {
            DRLPopulatorRunner.runDRL(drlContent, invalidJson);
        }, "Should throw RuntimeException for invalid JSON");
    }

    @Test
    public void testMissingTypeFieldHandling() {
        String drlContent = 
            "package org.drools.person;\n" +
            "declare Person\n" +
            "    name : String\n" +
            "    age : int\n" +
            "end";
        
        String jsonWithoutType = "{\"name\":\"John\", \"age\":25}";
        
        assertThrows(RuntimeException.class, () -> {
            DRLPopulatorRunner.runDRL(drlContent, jsonWithoutType);
        }, "Should throw RuntimeException when _type field is missing");
    }

    @Test
    public void testInvalidDRLHandling() {
        String invalidDRL = "invalid drl content";
        String jsonData = "{\"_type\":\"Person\", \"name\":\"Test\", \"age\":25}";
        
        assertThrows(RuntimeException.class, () -> {
            DRLPopulatorRunner.runDRL(invalidDRL, jsonData);
        }, "Should throw RuntimeException for invalid DRL");
    }

    @Test
    public void testFilterFactsByType() {
        String drlContent = 
            "package org.drools.person;\n" +
            "declare Person\n" +
            "    name : String\n" +
            "    age : int\n" +
            "end";
        
        String jsonData = "{\"_type\":\"Person\", \"name\":\"Alice\", \"age\":30}";
        
        DRLRunnerResult result = DRLPopulatorRunner.runDRL(drlContent, jsonData);
        List<Object> allFacts = result.objects();
        
        // Filter for Person facts
        List<Object> personFacts = DRLPopulatorRunner.filterFactsByType(allFacts, "Person");
        assertEquals(1, personFacts.size(), "Should have exactly 1 Person fact");
        
        // Test filtering for non-existent type
        List<Object> nonExistentFacts = DRLPopulatorRunner.filterFactsByType(allFacts, "NonExistentType");
        assertEquals(0, nonExistentFacts.size(), "Should have no facts for non-existent type");
    }

    @Test
    public void testComplexObjectWithMultipleFields() {
        String drlContent = 
            "package org.drools.product;\n" +
            "declare Product\n" +
            "    id : String\n" +
            "    name : String\n" +
            "    price : double\n" +
            "    category : String\n" +
            "    inStock : boolean = true\n" +
            "    discounted : boolean = false\n" +
            "end\n" +
            "rule 'Apply Discount'\n" +
            "when\n" +
            "    $p : Product(price > 100.0, discounted == false)\n" +
            "then\n" +
            "    modify($p) { setDiscounted(true); }\n" +
            "    System.out.println('Applied discount to ' + $p.getName());\n" +
            "end";
        
        String jsonData = "{\"_type\":\"Product\", \"id\":\"PROD001\", \"name\":\"Laptop\", \"price\":1500.0, \"category\":\"Electronics\"}";
        
        DRLRunnerResult result = DRLPopulatorRunner.runDRL(drlContent, jsonData);
        List<Object> facts = result.objects();
        
        assertNotNull(facts, "Facts should not be null");
        assertEquals(1, facts.size(), "Should have exactly 1 fact");
        
        Object product = facts.get(0);
        String productString = product.toString();
        assertTrue(productString.contains("PROD001"), "Product should have ID PROD001");
        assertTrue(productString.contains("Laptop"), "Product should have name Laptop");
        assertTrue(productString.contains("price=1500"), "Product should have price 1500");
        assertTrue(productString.contains("Electronics"), "Product should have category Electronics");
        assertTrue(productString.contains("inStock=true"), "Product should be in stock (default value)");
        assertTrue(productString.contains("discounted=true"), "Product should be discounted after rule execution");
    }
}