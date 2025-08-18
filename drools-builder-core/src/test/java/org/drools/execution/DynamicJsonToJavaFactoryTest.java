package org.drools.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.lang.reflect.Method;

/**
 * Tests for DynamicJsonToJavaFactory using reflection-based implementation.
 * Tests both DRL definitions and runtime class compilation.
 */
public class DynamicJsonToJavaFactoryTest {

    private DynamicJsonToJavaFactory factory;
    private org.drools.storage.DefinitionStorage definitionStorage;

    @BeforeEach
    public void setUp() {
        definitionStorage = new org.drools.storage.DefinitionStorage();
        factory = new DynamicJsonToJavaFactory(definitionStorage);
        
        setupSampleDefinitions();
    }
    
    private void setupSampleDefinitions() {
        String employeeDrl = """
            declare Employee
                name : String
                age : int
                department : String
                salary : double
            end
            """;
        definitionStorage.addDefinition("Employee", "declare", employeeDrl);
        
        String productDrl = """
            declare Product
                id : String
                name : String
                price : double
                inStock : boolean
            end
            """;
        definitionStorage.addDefinition("Product", "declare", productDrl);
        
        String customerDrl = """
            declare Customer
                customerId : String
                name : String
                email : String
                age : int
            end
            """;
        definitionStorage.addDefinition("Customer", "declare", customerDrl);
        
        String orderDrl = """
            declare Order
                orderId : String
                customerId : String
                amount : double
                status : String
            end
            """;
        definitionStorage.addDefinition("Order", "declare", orderDrl);
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testCreateEmployeesFromJson() {
        // Skip test if running in JRE without compiler
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String json = """
            [
                {"name": "John Smith", "age": 30, "department": "Engineering", "salary": 75000.0},
                {"name": "Jane Doe", "age": 28, "department": "Marketing", "salary": 65000.0},
                {"name": "Bob Wilson", "age": 35, "department": "Sales", "salary": 70000.0}
            ]
            """;

        List<Object> employees = factory.createObjectsFromJson(json, "Employee");
        
        assertNotNull(employees);
        assertEquals(3, employees.size());
        
        for (Object employee : employees) {
            assertNotNull(employee);
            
            // Test reflection-based access
            assertNotNull(getFieldValue(employee, "name"));
            assertNotNull(getFieldValue(employee, "age"));
            assertNotNull(getFieldValue(employee, "department"));
            assertNotNull(getFieldValue(employee, "salary"));
            
            // Verify toString contains expected content
            String str = employee.toString();
            assertTrue(str.contains("Employee{"));
            assertTrue(str.contains("name="));
            assertTrue(str.contains("age="));
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testCreateSingleProductFromJson() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String json = """
            {"id": "PROD-001", "name": "Laptop Computer", "price": 1299.99, "inStock": true}
            """;

        List<Object> products = factory.createObjectsFromJson(json, "Product");
        
        assertNotNull(products);
        assertEquals(1, products.size());
        
        Object product = products.get(0);
        assertNotNull(product);
        
        assertEquals("PROD-001", getFieldValue(product, "id"));
        assertEquals("Laptop Computer", getFieldValue(product, "name"));
        assertEquals(1299.99, (Double) getFieldValue(product, "price"), 0.01);
        assertEquals(true, getFieldValue(product, "inStock"));
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testCreateCustomersFromJson() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String json = """
            [
                {"customerId": "CUST-001", "name": "Alice Johnson", "email": "alice@example.com", "age": 32},
                {"customerId": "CUST-002", "name": "Bob Smith", "email": "bob@test.com", "age": 28}
            ]
            """;

        List<Object> customers = factory.createObjectsFromJson(json, "Customer");
        
        assertNotNull(customers);
        assertEquals(2, customers.size());
        
        Object customer1 = customers.get(0);
        assertEquals("CUST-001", getFieldValue(customer1, "customerId"));
        assertEquals("Alice Johnson", getFieldValue(customer1, "name"));
        assertEquals("alice@example.com", getFieldValue(customer1, "email"));
        assertEquals(32, getFieldValue(customer1, "age"));
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testReflectionBasedFieldAccess() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String json = """
            {"name": "Test Employee", "age": 25, "department": "IT", "salary": 60000.0}
            """;

        List<Object> employees = factory.createObjectsFromJson(json, "Employee");
        assertEquals(1, employees.size());
        
        Object employee = employees.get(0);
        
        // Test getter methods using reflection
        assertEquals("Test Employee", invokeGetter(employee, "getName"));
        assertEquals(25, invokeGetter(employee, "getAge"));
        assertEquals("IT", invokeGetter(employee, "getDepartment"));
        assertEquals(60000.0, (Double) invokeGetter(employee, "getSalary"), 0.01);
        
        // Test setter methods using reflection
        invokeSetter(employee, "setName", "Updated Name");
        assertEquals("Updated Name", invokeGetter(employee, "getName"));
        
        invokeSetter(employee, "setAge", 30);
        assertEquals(30, invokeGetter(employee, "getAge"));
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testMixedObjectsWithExplicitTypes() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String json = """
            [
                {"type": "Employee", "name": "Mixed Employee", "age": 40, "department": "HR", "salary": 55000.0},
                {"type": "Product", "id": "MIX-001", "name": "Mixed Product", "price": 199.99, "inStock": true},
                {"type": "Customer", "customerId": "MIX-CUST", "name": "Mixed Customer", "email": "mixed@test.com", "age": 35}
            ]
            """;

        List<Object> objects = factory.createMixedObjectsFromJson(json);
        
        assertNotNull(objects);
        assertEquals(3, objects.size());
        
        assertTrue(objects.get(0).getClass().getSimpleName().equals("Employee"));
        assertTrue(objects.get(1).getClass().getSimpleName().equals("Product"));
        assertTrue(objects.get(2).getClass().getSimpleName().equals("Customer"));
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testDynamicClassCreation() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String vehicleDrl = """
            declare Vehicle
                make : String
                model : String
                year : int
                price : double
            end
            """;
        definitionStorage.addDefinition("Vehicle", "declare", vehicleDrl);
        
        String json = """
            {"make": "Toyota", "model": "Camry", "year": 2023, "price": 25000.0}
            """;

        List<Object> vehicles = factory.createObjectsFromJson(json, "Vehicle");
        
        assertNotNull(vehicles);
        assertEquals(1, vehicles.size());
        
        Object vehicle = vehicles.get(0);
        assertEquals("Toyota", getFieldValue(vehicle, "make"));
        assertEquals("Camry", getFieldValue(vehicle, "model"));
        assertEquals(2023, getFieldValue(vehicle, "year"));
        assertEquals(25000.0, (Double) getFieldValue(vehicle, "price"), 0.01);
        
        // Verify the class was compiled dynamically
        assertTrue(vehicle.getClass().getName().contains("org.drools.execution.dynamic.Vehicle"));
    }

    @Test
    public void testInvalidDefinitionType() {
        String json = """
            {"name": "Test", "value": "Something"}
            """;
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.createObjectsFromJson(json, "NonExistentType");
        });
    }

    @Test
    public void testInvalidJson() {
        String invalidJson = "{ invalid json structure }";
        
        assertThrows(RuntimeException.class, () -> {
            factory.createObjectsFromJson(invalidJson, "Employee");
        });
    }

    @Test
    public void testEmptyArray() {
        String emptyJson = "[]";
        
        List<Object> objects = factory.createObjectsFromJson(emptyJson, "Employee");
        
        assertNotNull(objects);
        assertTrue(objects.isEmpty());
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testComplexDataTypes() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String bookDrl = """
            declare Book
                isbn : String
                title : String
                author : String
                pages : int
                published : boolean
            end
            """;
        definitionStorage.addDefinition("Book", "declare", bookDrl);
        
        String json = """
            [
                {"isbn": "978-0123456789", "title": "Java Programming", "author": "John Author", "pages": 450, "published": true},
                {"isbn": "978-9876543210", "title": "Advanced Drools", "author": "Jane Expert", "pages": 320, "published": false}
            ]
            """;

        List<Object> books = factory.createObjectsFromJson(json, "Book");
        
        assertNotNull(books);
        assertEquals(2, books.size());
        
        Object book1 = books.get(0);
        assertEquals("978-0123456789", getFieldValue(book1, "isbn"));
        assertEquals("Java Programming", getFieldValue(book1, "title"));
        assertEquals("John Author", getFieldValue(book1, "author"));
        assertEquals(450, getFieldValue(book1, "pages"));
        assertEquals(true, getFieldValue(book1, "published"));
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testClassCaching() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String json1 = """
            {"name": "Employee 1", "age": 25, "department": "IT", "salary": 50000.0}
            """;
        
        String json2 = """
            {"name": "Employee 2", "age": 30, "department": "HR", "salary": 55000.0}
            """;

        List<Object> employees1 = factory.createObjectsFromJson(json1, "Employee");
        List<Object> employees2 = factory.createObjectsFromJson(json2, "Employee");
        
        // Verify both objects are of the same class (cached)
        assertSame(employees1.get(0).getClass(), employees2.get(0).getClass());
    }

    @Test
    @EnabledIfSystemProperty(named = "java.specification.name", matches = ".*")
    public void testObjectEquality() {
        try {
            javax.tools.ToolProvider.getSystemJavaCompiler();
        } catch (Exception e) {
            System.out.println("Skipping test - JDK compiler not available");
            return;
        }

        String json = """
            [
                {"name": "John Doe", "age": 30, "department": "Engineering", "salary": 75000.0},
                {"name": "John Doe", "age": 30, "department": "Engineering", "salary": 75000.0}
            ]
            """;

        List<Object> employees = factory.createObjectsFromJson(json, "Employee");
        
        assertEquals(2, employees.size());
        
        // Objects should have same field values but be different instances
        assertNotSame(employees.get(0), employees.get(1));
        assertEquals(getFieldValue(employees.get(0), "name"), getFieldValue(employees.get(1), "name"));
        assertEquals(getFieldValue(employees.get(0), "age"), getFieldValue(employees.get(1), "age"));
    }
    
    @Test
    public void testDefinitionStorageContainsExpectedDefinitions() {
        assertTrue(definitionStorage.hasDefinition("Employee"));
        assertTrue(definitionStorage.hasDefinition("Product"));
        assertTrue(definitionStorage.hasDefinition("Customer"));
        assertTrue(definitionStorage.hasDefinition("Order"));
        
        assertEquals(4, definitionStorage.getDefinitionCount());
        
        List<org.drools.storage.DefinitionStorage.DroolsDefinition> declares = definitionStorage.getDefinitionsByType("declare");
        assertEquals(4, declares.size());
    }

    // Helper methods for reflection-based testing
    
    private Object getFieldValue(Object obj, String fieldName) {
        try {
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method getter = obj.getClass().getMethod(getterName);
            return getter.invoke(obj);
        } catch (Exception e) {
            fail("Failed to get field value for " + fieldName + ": " + e.getMessage());
            return null;
        }
    }
    
    private Object invokeGetter(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        } catch (Exception e) {
            fail("Failed to invoke getter " + methodName + ": " + e.getMessage());
            return null;
        }
    }
    
    private void invokeSetter(Object obj, String methodName, Object value) {
        try {
            Class<?> paramType = value.getClass();
            if (paramType == Integer.class) paramType = int.class;
            if (paramType == Double.class) paramType = double.class;
            if (paramType == Boolean.class) paramType = boolean.class;
            
            Method method = obj.getClass().getMethod(methodName, paramType);
            method.invoke(obj, value);
        } catch (Exception e) {
            fail("Failed to invoke setter " + methodName + ": " + e.getMessage());
        }
    }
}