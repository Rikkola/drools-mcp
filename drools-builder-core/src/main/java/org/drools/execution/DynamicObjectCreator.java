package org.drools.execution;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * DynamicObjectCreator integrates JShell to dynamically create Java objects from code strings.
 * This allows for runtime creation of objects without pre-compiled classes.
 */
public class DynamicObjectCreator {
    
    private static final Map<String, String> CLASS_CACHE = new HashMap<>();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\s*(\\w+)\\s+(\\w+)\\s*=");
    
    /**
     * Creates an object dynamically from a Java code string using JShell.
     * 
     * @param code Java code string that creates an object (e.g., "Person person = new Person(\"John\", 30);")
     * @param classDefinitions Optional map of class name to class definition code
     * @return The created object, or null if creation failed
     */
    public Object createObjectFromString(String code, Map<String, String> classDefinitions) {
        JShell jshell = null;
        try {
            jshell = JShell.create();
            
            // Add class definitions if provided
            if (classDefinitions != null) {
                for (Map.Entry<String, String> entry : classDefinitions.entrySet()) {
                    List<SnippetEvent> events = jshell.eval(entry.getValue());
                    checkForErrors(events, "Class definition for " + entry.getKey());
                }
            }
            
            // Execute the object creation code
            List<SnippetEvent> events = jshell.eval(code);
            checkForErrors(events, "Object creation");
            
            // Extract variable name and retrieve the object
            String varName = extractVariableName(code);
            if (varName != null) {
                List<SnippetEvent> valueEvents = jshell.eval(varName);
                if (!valueEvents.isEmpty() && valueEvents.get(0).value() != null) {
                    return valueEvents.get(0).value();
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create object from code: " + e.getMessage(), e);
        } finally {
            if (jshell != null) {
                jshell.close();
            }
        }
        
        return null;
    }
    
    /**
     * Creates an object using a predefined class template.
     * 
     * @param className The name of the class (e.g., "Person")
     * @param constructorArgs Arguments for the constructor
     * @return The created object
     */
    public Object createObjectWithTemplate(String className, Object... constructorArgs) {
        String classDefinition = getClassDefinition(className);
        if (classDefinition == null) {
            throw new IllegalArgumentException("No class definition found for: " + className);
        }
        
        // Build constructor call
        StringBuilder constructorCall = new StringBuilder();
        constructorCall.append(className).append(" obj = new ").append(className).append("(");
        
        for (int i = 0; i < constructorArgs.length; i++) {
            if (i > 0) constructorCall.append(", ");
            
            Object arg = constructorArgs[i];
            if (arg instanceof String) {
                constructorCall.append("\"").append(arg.toString().replace("\"", "\\\"")).append("\"");
            } else if (arg instanceof Number || arg instanceof Boolean) {
                constructorCall.append(arg.toString());
            } else {
                constructorCall.append("\"").append(arg.toString()).append("\"");
            }
        }
        
        constructorCall.append(");");
        
        Map<String, String> classDefinitions = new HashMap<>();
        classDefinitions.put(className, classDefinition);
        
        return createObjectFromString(constructorCall.toString(), classDefinitions);
    }
    
    /**
     * Creates a Person object with name and age.
     * This is a convenience method for the most common use case.
     */
    public Object createPerson(String name, int age) {
        String personClassDef = getPersonClassDefinition();
        Map<String, String> classDefinitions = new HashMap<>();
        classDefinitions.put("Person", personClassDef);
        
        String code = String.format("Person person = new Person(\"%s\", %d);", 
                                    name.replace("\"", "\\\""), age);
        
        return createObjectFromString(code, classDefinitions);
    }
    
    /**
     * Creates multiple objects from a list of code strings.
     */
    public Object[] createObjects(List<String> codes, Map<String, String> classDefinitions) {
        JShell jshell = null;
        try {
            jshell = JShell.create();
            
            // Add class definitions
            if (classDefinitions != null) {
                for (Map.Entry<String, String> entry : classDefinitions.entrySet()) {
                    List<SnippetEvent> events = jshell.eval(entry.getValue());
                    checkForErrors(events, "Class definition for " + entry.getKey());
                }
            }
            
            Object[] results = new Object[codes.size()];
            
            // Execute each code snippet
            for (int i = 0; i < codes.size(); i++) {
                String code = codes.get(i);
                List<SnippetEvent> events = jshell.eval(code);
                checkForErrors(events, "Object creation " + (i + 1));
                
                // Extract variable name and retrieve the object
                String varName = extractVariableName(code);
                if (varName != null) {
                    List<SnippetEvent> valueEvents = jshell.eval(varName);
                    if (!valueEvents.isEmpty() && valueEvents.get(0).value() != null) {
                        results[i] = valueEvents.get(0).value();
                    }
                }
            }
            
            return results;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create objects: " + e.getMessage(), e);
        } finally {
            if (jshell != null) {
                jshell.close();
            }
        }
    }
    
    /**
     * Evaluates any Java expression and returns the result.
     */
    public Object evaluateExpression(String expression, Map<String, String> classDefinitions) {
        JShell jshell = null;
        try {
            jshell = JShell.create();
            
            // Add class definitions
            if (classDefinitions != null) {
                for (Map.Entry<String, String> entry : classDefinitions.entrySet()) {
                    List<SnippetEvent> events = jshell.eval(entry.getValue());
                    checkForErrors(events, "Class definition for " + entry.getKey());
                }
            }
            
            // Evaluate the expression
            List<SnippetEvent> events = jshell.eval(expression);
            checkForErrors(events, "Expression evaluation");
            
            if (!events.isEmpty() && events.get(0).value() != null) {
                return events.get(0).value();
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate expression: " + e.getMessage(), e);
        } finally {
            if (jshell != null) {
                jshell.close();
            }
        }
        
        return null;
    }
    
    /**
     * Registers a class definition for later use.
     */
    public static void registerClassDefinition(String className, String classDefinition) {
        CLASS_CACHE.put(className, classDefinition);
    }
    
    /**
     * Gets a registered class definition.
     */
    public static String getClassDefinition(String className) {
        return CLASS_CACHE.get(className);
    }
    
    /**
     * Gets all registered class definitions.
     */
    public static Map<String, String> getAllClassDefinitions() {
        return new HashMap<>(CLASS_CACHE);
    }
    
    /**
     * Clears all registered class definitions.
     */
    public static void clearClassDefinitions() {
        CLASS_CACHE.clear();
    }
    
    // Static block to register common class definitions
    static {
        registerClassDefinition("Person", getPersonClassDefinition());
        registerClassDefinition("Address", getAddressClassDefinition());
        registerClassDefinition("Order", getOrderClassDefinition());
    }
    
    /**
     * Extracts the variable name from a Java assignment statement.
     */
    private String extractVariableName(String code) {
        Matcher matcher = VARIABLE_PATTERN.matcher(code);
        if (matcher.find()) {
            return matcher.group(2); // Return the variable name
        }
        return null;
    }
    
    /**
     * Checks JShell events for errors and throws exceptions if found.
     */
    private void checkForErrors(List<SnippetEvent> events, String context) {
        for (SnippetEvent event : events) {
            if (event.exception() != null) {
                throw new RuntimeException(context + " failed: " + event.exception().getMessage(), 
                                         event.exception());
            }
            if (event.status() == jdk.jshell.Snippet.Status.REJECTED) {
                throw new RuntimeException(context + " was rejected by JShell");
            }
        }
    }
    
    /**
     * Standard Person class definition.
     */
    private static String getPersonClassDefinition() {
        return """
            class Person {
                private String name;
                private int age;
                private String email;
                
                public Person(String name, int age) {
                    this.name = name;
                    this.age = age;
                }
                
                public Person(String name, int age, String email) {
                    this.name = name;
                    this.age = age;
                    this.email = email;
                }
                
                public String getName() { return name; }
                public int getAge() { return age; }
                public String getEmail() { return email; }
                public void setName(String name) { this.name = name; }
                public void setAge(int age) { this.age = age; }
                public void setEmail(String email) { this.email = email; }
                
                public boolean isAdult() { return age >= 18; }
                public boolean isSenior() { return age >= 65; }
                
                @Override
                public String toString() {
                    return "Person{name='" + name + "', age=" + age + 
                           (email != null ? ", email='" + email + "'" : "") + "}";
                }
                
                @Override
                public boolean equals(Object obj) {
                    if (this == obj) return true;
                    if (obj == null || getClass() != obj.getClass()) return false;
                    Person person = (Person) obj;
                    return age == person.age && 
                           java.util.Objects.equals(name, person.name) && 
                           java.util.Objects.equals(email, person.email);
                }
                
                @Override
                public int hashCode() {
                    return java.util.Objects.hash(name, age, email);
                }
            }
            """;
    }
    
    /**
     * Standard Address class definition.
     */
    private static String getAddressClassDefinition() {
        return """
            class Address {
                private String street;
                private String city;
                private String zipCode;
                private String country;
                
                public Address(String street, String city, String zipCode) {
                    this.street = street;
                    this.city = city;
                    this.zipCode = zipCode;
                    this.country = "USA"; // default
                }
                
                public Address(String street, String city, String zipCode, String country) {
                    this.street = street;
                    this.city = city;
                    this.zipCode = zipCode;
                    this.country = country;
                }
                
                public String getStreet() { return street; }
                public String getCity() { return city; }
                public String getZipCode() { return zipCode; }
                public String getCountry() { return country; }
                
                public void setStreet(String street) { this.street = street; }
                public void setCity(String city) { this.city = city; }
                public void setZipCode(String zipCode) { this.zipCode = zipCode; }
                public void setCountry(String country) { this.country = country; }
                
                @Override
                public String toString() {
                    return "Address{street='" + street + "', city='" + city + 
                           "', zipCode='" + zipCode + "', country='" + country + "'}";
                }
            }
            """;
    }
    
    /**
     * Standard Order class definition.
     */
    private static String getOrderClassDefinition() {
        return """
            class Order {
                private String orderId;
                private double amount;
                private String status;
                private String customerId;
                
                public Order(String orderId, double amount, String customerId) {
                    this.orderId = orderId;
                    this.amount = amount;
                    this.customerId = customerId;
                    this.status = "PENDING";
                }
                
                public String getOrderId() { return orderId; }
                public double getAmount() { return amount; }
                public String getStatus() { return status; }
                public String getCustomerId() { return customerId; }
                
                public void setStatus(String status) { this.status = status; }
                public void setAmount(double amount) { this.amount = amount; }
                
                public boolean isHighValue() { return amount > 1000.0; }
                public boolean isPending() { return "PENDING".equals(status); }
                public boolean isCompleted() { return "COMPLETED".equals(status); }
                
                @Override
                public String toString() {
                    return "Order{orderId='" + orderId + "', amount=" + amount + 
                           ", status='" + status + "', customerId='" + customerId + "'}";
                }
            }
            """;
    }
}
