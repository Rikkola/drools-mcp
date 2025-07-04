package org.drools;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for DynamicObjectFactory
 */
class DynamicObjectFactoryTest {

    private DynamicObjectFactory factory;
    private ObjectDefinition personDefinition;
    private ObjectDefinition addressDefinition;

    @BeforeEach
    void setUp() {
        factory = new DynamicObjectFactory();
        
        // Create a Person object definition
        personDefinition = new ObjectDefinition("Person", "com.example.model")
                .addField("id", FieldDefinition.FieldType.LONG, true)
                .addField("name", FieldDefinition.FieldType.STRING, true)
                .addField("age", FieldDefinition.FieldType.INTEGER, false)
                .addField("email", FieldDefinition.FieldType.STRING, false)
                .addField("active", FieldDefinition.FieldType.BOOLEAN, false)
                .addField("scores", FieldDefinition.FieldType.LIST, false)
                .addField("metadata", FieldDefinition.FieldType.MAP, false);

        // Create an Address object definition  
        addressDefinition = new ObjectDefinition("Address", "com.example.model")
                .addField("street", FieldDefinition.FieldType.STRING, true)
                .addField("city", FieldDefinition.FieldType.STRING, true)
                .addField("zipCode", FieldDefinition.FieldType.STRING, false)
                .addField("country", FieldDefinition.FieldType.STRING, false);
    }

    @Nested
    @DisplayName("Object Definition Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register single object definition successfully")
        void testRegisterObjectDefinition() {
            factory.registerObjectDefinition(personDefinition);
            
            assertTrue(factory.hasObjectDefinition("Person"));
            assertEquals(personDefinition, factory.getObjectDefinition("Person"));
            assertEquals(1, factory.getRegisteredDefinitionCount());
        }

        @Test
        @DisplayName("Should register multiple object definitions successfully")
        void testRegisterMultipleObjectDefinitions() {
            List<ObjectDefinition> definitions = Arrays.asList(personDefinition, addressDefinition);
            factory.registerObjectDefinitions(definitions);
            
            assertTrue(factory.hasObjectDefinition("Person"));
            assertTrue(factory.hasObjectDefinition("Address"));
            assertEquals(2, factory.getRegisteredDefinitionCount());
            
            Set<String> expectedNames = Set.of("Person", "Address");
            assertEquals(expectedNames, factory.getRegisteredDefinitionNames());
        }

        @Test
        @DisplayName("Should throw exception when registering null definition")
        void testRegisterNullObjectDefinition() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.registerObjectDefinition(null)
            );
            assertEquals("Object definition cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when registering definition with null name")
        void testRegisterObjectDefinitionWithNullName() {
            ObjectDefinition invalidDefinition = new ObjectDefinition();
            invalidDefinition.setName(null);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.registerObjectDefinition(invalidDefinition)
            );
            assertEquals("Object definition name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when registering definition with empty name")
        void testRegisterObjectDefinitionWithEmptyName() {
            ObjectDefinition invalidDefinition = new ObjectDefinition();
            invalidDefinition.setName("   ");
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.registerObjectDefinition(invalidDefinition)
            );
            assertEquals("Object definition name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when registering null collection")
        void testRegisterNullObjectDefinitions() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.registerObjectDefinitions(null)
            );
            assertEquals("Object definitions cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should overwrite existing definition with same name")
        void testOverwriteExistingDefinition() {
            factory.registerObjectDefinition(personDefinition);
            assertEquals(1, factory.getRegisteredDefinitionCount());
            
            ObjectDefinition newPersonDefinition = new ObjectDefinition("Person", "com.example.other")
                    .addField("differentField", FieldDefinition.FieldType.STRING, true);
            
            factory.registerObjectDefinition(newPersonDefinition);
            assertEquals(1, factory.getRegisteredDefinitionCount());
            assertEquals(newPersonDefinition, factory.getObjectDefinition("Person"));
        }
    }

    @Nested
    @DisplayName("Object Creation from Map Tests")
    class MapCreationTests {

        @BeforeEach
        void setUp() {
            factory.registerObjectDefinition(personDefinition);
        }

        @Test
        @DisplayName("Should create object from valid map data")
        void testCreateFromValidMap() throws Exception {
            Map<String, Object> personData = new HashMap<>();
            personData.put("id", 1L);
            personData.put("name", "John Doe");
            personData.put("age", 30);
            personData.put("email", "john@example.com");
            personData.put("active", true);
            personData.put("scores", Arrays.asList(85, 90, 78));
            personData.put("metadata", Map.of("level", "senior"));

            DynamicObjectFactory.DynamicObject person = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", personData);

            assertNotNull(person);
            assertEquals("Person", person.getObjectTypeName());
            assertEquals(1L, person.getFieldValue("id"));
            assertEquals("John Doe", person.getFieldValue("name"));
            assertEquals(30, person.getFieldValue("age"));
            assertEquals("john@example.com", person.getFieldValue("email"));
            assertEquals(true, person.getFieldValue("active"));
            assertEquals(Arrays.asList(85, 90, 78), person.getFieldValue("scores"));
            assertEquals(Map.of("level", "senior"), person.getFieldValue("metadata"));
        }

        @Test
        @DisplayName("Should create object with default values for missing optional fields")
        void testCreateFromMapWithDefaults() throws Exception {
            FieldDefinition fieldWithDefault = new FieldDefinition("status", FieldDefinition.FieldType.STRING, false);
            fieldWithDefault.setDefaultValue("active");
            
            ObjectDefinition defWithDefaults = new ObjectDefinition("TestObject")
                    .addField("id", FieldDefinition.FieldType.LONG, true)
                    .addField(fieldWithDefault);
            
            factory.registerObjectDefinition(defWithDefaults);
            
            Map<String, Object> data = Map.of("id", 1L);
            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("TestObject", data);

            assertEquals("active", obj.getFieldValue("status"));
        }

        @Test
        @DisplayName("Should throw exception for missing required fields")
        void testCreateFromMapMissingRequiredFields() {
            Map<String, Object> incompleteData = new HashMap<>();
            incompleteData.put("name", "John Doe");
            // Missing required 'id' field

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createFromMap("Person", incompleteData)
            );
            assertTrue(exception.getMessage().contains("Missing required fields"));
            assertTrue(exception.getMessage().contains("id"));
        }

        @Test
        @DisplayName("Should throw exception for unknown object definition")
        void testCreateFromMapUnknownDefinition() {
            Map<String, Object> data = Map.of("field", "value");
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createFromMap("UnknownType", data)
            );
            assertEquals("Object definition not found: UnknownType", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Object Creation from JSON Tests")
    class JsonCreationTests {

        @BeforeEach
        void setUp() {
            factory.registerObjectDefinition(personDefinition);
        }

        @Test
        @DisplayName("Should create object from valid JSON")
        void testCreateFromValidJson() throws Exception {
            String jsonData = """
                {
                    "id": 1,
                    "name": "Jane Smith",
                    "age": 25,
                    "email": "jane@example.com",
                    "active": true,
                    "scores": [95, 88, 92],
                    "metadata": {"department": "engineering"}
                }
                """;

            DynamicObjectFactory.DynamicObject person = 
                (DynamicObjectFactory.DynamicObject) factory.createFromJson("Person", jsonData);

            assertNotNull(person);
            assertEquals("Person", person.getObjectTypeName());
            assertEquals(1L, person.getFieldValue("id")); 
            assertEquals("Jane Smith", person.getFieldValue("name"));
            assertEquals(25, person.getFieldValue("age"));
            assertEquals("jane@example.com", person.getFieldValue("email"));
            assertEquals(true, person.getFieldValue("active"));
            
            @SuppressWarnings("unchecked")
            List<Integer> scores = (List<Integer>) person.getFieldValue("scores");
            assertEquals(Arrays.asList(95, 88, 92), scores);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) person.getFieldValue("metadata");
            assertEquals("engineering", metadata.get("department"));
        }

        @Test
        @DisplayName("Should create multiple objects from JSON array")
        void testCreateMultipleFromJsonArray() throws Exception {
            String jsonArrayData = """
                [
                    {
                        "id": 1,
                        "name": "Alice",
                        "age": 30
                    },
                    {
                        "id": 2,
                        "name": "Bob",
                        "age": 25
                    }
                ]
                """;

            List<Object> people = factory.createMultipleFromJson("Person", jsonArrayData);

            assertEquals(2, people.size());
            
            DynamicObjectFactory.DynamicObject alice = (DynamicObjectFactory.DynamicObject) people.get(0);
            assertEquals("Alice", alice.getFieldValue("name"));
            assertEquals(1L, alice.getFieldValue("id"));
            
            DynamicObjectFactory.DynamicObject bob = (DynamicObjectFactory.DynamicObject) people.get(1);
            assertEquals("Bob", bob.getFieldValue("name"));
            assertEquals(2L, bob.getFieldValue("id"));
        }

        @Test
        @DisplayName("Should throw exception for invalid JSON")
        void testCreateFromInvalidJson() {
            String invalidJson = "{ invalid json }";
            
            assertThrows(Exception.class, () -> factory.createFromJson("Person", invalidJson));
        }

        @Test
        @DisplayName("Should throw exception for unknown definition in JSON creation")
        void testCreateFromJsonUnknownDefinition() {
            String jsonData = "{\"field\": \"value\"}";
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.createFromJson("UnknownType", jsonData)
            );
            assertEquals("Object definition not found: UnknownType", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Type Conversion Tests")
    class TypeConversionTests {

        @BeforeEach
        void setUp() {
            factory.registerObjectDefinition(personDefinition);
        }

        @Test
        @DisplayName("Should convert string values to correct types")
        void testTypeConversions() throws Exception {
            Map<String, Object> data = new HashMap<>();
            data.put("id", "123");  // String that should convert to Long
            data.put("name", "Test User");
            data.put("age", "30");  // String that should convert to Integer
            data.put("active", "true");  // String that should convert to Boolean

            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", data);

            assertEquals(123L, obj.getFieldValue("id"));
            assertEquals(30, obj.getFieldValue("age"));
            assertEquals(true, obj.getFieldValue("active"));
        }

        @Test
        @DisplayName("Should handle numeric type conversions")
        void testNumericConversions() throws Exception {
            ObjectDefinition numericDef = new ObjectDefinition("NumericTest")
                    .addField("longValue", FieldDefinition.FieldType.LONG, true)
                    .addField("intValue", FieldDefinition.FieldType.INTEGER, true)
                    .addField("doubleValue", FieldDefinition.FieldType.DOUBLE, true);
            
            factory.registerObjectDefinition(numericDef);
            
            Map<String, Object> data = new HashMap<>();
            data.put("longValue", 42.5);  // Double to Long
            data.put("intValue", 123.7);  // Double to Integer
            data.put("doubleValue", 456);  // Integer to Double

            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("NumericTest", data);

            assertEquals(42L, obj.getFieldValue("longValue"));
            assertEquals(123, obj.getFieldValue("intValue"));
            assertEquals(456.0, obj.getFieldValue("doubleValue"));
        }

        @Test
        @DisplayName("Should throw exception for invalid type conversion")
        void testInvalidTypeConversion() {
            Map<String, Object> data = new HashMap<>();
            data.put("id", 123L);
            data.put("name", "Test");
            data.put("age", "invalid_number");  // Invalid integer conversion

            assertThrows(RuntimeException.class, 
                () -> factory.createFromMap("Person", data));
        }
    }

    @Nested
    @DisplayName("Dynamic Object Interface Tests")
    class DynamicObjectInterfaceTests {

        @BeforeEach
        void setUp() {
            factory.registerObjectDefinition(personDefinition);
        }

        @Test
        @DisplayName("Should support getter and setter methods")
        void testGetterSetterMethods() throws Exception {
            Map<String, Object> data = Map.of("id", 1L, "name", "Test User");
            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", data);

            // Test field access
            assertEquals(1L, obj.getFieldValue("id"));
            assertEquals("Test User", obj.getFieldValue("name"));

            // Test field modification
            obj.setFieldValue("name", "Updated Name");
            assertEquals("Updated Name", obj.getFieldValue("name"));

            // Test new field addition
            obj.setFieldValue("newField", "newValue");
            assertEquals("newValue", obj.getFieldValue("newField"));
        }

        @Test
        @DisplayName("Should return correct object type name")
        void testGetObjectTypeName() throws Exception {
            Map<String, Object> data = Map.of("id", 1L, "name", "Test");
            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", data);

            assertEquals("Person", obj.getObjectTypeName());
        }

        @Test
        @DisplayName("Should return all field values")
        void testGetAllFieldValues() throws Exception {
            Map<String, Object> originalData = Map.of(
                "id", 1L, 
                "name", "Test User",
                "age", 30
            );
            
            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", originalData);

            Map<String, Object> allFields = obj.getAllFieldValues();
            
            assertEquals(1L, allFields.get("id"));
            assertEquals("Test User", allFields.get("name"));
            assertEquals(30, allFields.get("age"));
            assertNull(allFields.get("email")); // Optional field not set
        }

        @Test
        @DisplayName("Should return correct object definition")
        void testGetObjectDefinition() throws Exception {
            Map<String, Object> data = Map.of("id", 1L, "name", "Test");
            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", data);

            assertEquals(personDefinition, obj.getObjectDefinition());
        }
    }

    @Nested
    @DisplayName("Nested Object Tests")
    class NestedObjectTests {

        @Test
        @DisplayName("Should create nested objects")
        void testNestedObjectCreation() throws Exception {
            // Create a Person definition with Address field
            FieldDefinition addressField = new FieldDefinition("address", FieldDefinition.FieldType.OBJECT, true);
            addressField.setObjectTypeName("Address");
            
            ObjectDefinition personWithAddress = new ObjectDefinition("PersonWithAddress")
                    .addField("id", FieldDefinition.FieldType.LONG, true)
                    .addField("name", FieldDefinition.FieldType.STRING, true)
                    .addField(addressField);

            factory.registerObjectDefinition(addressDefinition);
            factory.registerObjectDefinition(personWithAddress);

            Map<String, Object> personData = new HashMap<>();
            personData.put("id", 1L);
            personData.put("name", "John Doe");
            
            Map<String, Object> addressData = new HashMap<>();
            addressData.put("street", "123 Main St");
            addressData.put("city", "Springfield");
            addressData.put("zipCode", "12345");
            personData.put("address", addressData);

            DynamicObjectFactory.DynamicObject person = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("PersonWithAddress", personData);

            assertNotNull(person);
            assertEquals("John Doe", person.getFieldValue("name"));
            
            DynamicObjectFactory.DynamicObject address = 
                (DynamicObjectFactory.DynamicObject) person.getFieldValue("address");
            assertNotNull(address);
            assertEquals("Address", address.getObjectTypeName());
            assertEquals("123 Main St", address.getFieldValue("street"));
            assertEquals("Springfield", address.getFieldValue("city"));
        }
    }

    @Nested
    @DisplayName("Factory Management Tests")
    class FactoryManagementTests {

        @Test
        @DisplayName("Should clear all definitions")
        void testClearAll() {
            factory.registerObjectDefinition(personDefinition);
            factory.registerObjectDefinition(addressDefinition);
            assertEquals(2, factory.getRegisteredDefinitionCount());

            factory.clearAll();
            assertEquals(0, factory.getRegisteredDefinitionCount());
            assertFalse(factory.hasObjectDefinition("Person"));
            assertFalse(factory.hasObjectDefinition("Address"));
        }

        @Test
        @DisplayName("Should return correct registered definition count")
        void testGetRegisteredDefinitionCount() {
            assertEquals(0, factory.getRegisteredDefinitionCount());
            
            factory.registerObjectDefinition(personDefinition);
            assertEquals(1, factory.getRegisteredDefinitionCount());
            
            factory.registerObjectDefinition(addressDefinition);
            assertEquals(2, factory.getRegisteredDefinitionCount());
        }

        @Test
        @DisplayName("Should return appropriate summary")
        void testGetSummary() {
            // Test empty factory
            String emptySummary = factory.getSummary();
            assertEquals("No object definitions registered.", emptySummary);
            
            // Test with definitions
            factory.registerObjectDefinition(personDefinition);
            factory.registerObjectDefinition(addressDefinition);
            
            String summary = factory.getSummary();
            assertTrue(summary.contains("Registered Object Definitions (2)"));
            assertTrue(summary.contains("Person"));
            assertTrue(summary.contains("Address"));
        }

        @Test
        @DisplayName("Should return empty set for no registered definitions")
        void testGetRegisteredDefinitionNamesEmpty() {
            Set<String> names = factory.getRegisteredDefinitionNames();
            assertTrue(names.isEmpty());
        }

        @Test
        @DisplayName("Should return null for non-existent definition")
        void testGetNonExistentObjectDefinition() {
            assertNull(factory.getObjectDefinition("NonExistent"));
            assertFalse(factory.hasObjectDefinition("NonExistent"));
        }
    }

    @Nested
    @DisplayName("Proxy Object Behavior Tests")
    class ProxyObjectBehaviorTests {

        @BeforeEach
        void setUp() {
            factory.registerObjectDefinition(personDefinition);
        }

        @Test
        @DisplayName("Should support toString method")
        void testToStringMethod() throws Exception {
            Map<String, Object> data = Map.of("id", 1L, "name", "Test User");
            Object obj = factory.createFromMap("Person", data);

            String toString = obj.toString();
            assertTrue(toString.contains("Person"));
            assertTrue(toString.contains("id"));
            assertTrue(toString.contains("name"));
        }

        @Test
        @DisplayName("Should support equals method")
        void testEqualsMethod() throws Exception {
            Map<String, Object> data1 = Map.of("id", 1L, "name", "Test User");
            Map<String, Object> data2 = Map.of("id", 1L, "name", "Test User");
            Map<String, Object> data3 = Map.of("id", 2L, "name", "Other User");

            Object obj1 = factory.createFromMap("Person", data1);
            Object obj2 = factory.createFromMap("Person", data2);
            Object obj3 = factory.createFromMap("Person", data3);

            assertEquals(obj1, obj2);
            assertNotEquals(obj1, obj3);
            assertNotEquals(obj1, null);
            assertNotEquals(obj1, "not a dynamic object");
        }

        @Test
        @DisplayName("Should support hashCode method")
        void testHashCodeMethod() throws Exception {
            Map<String, Object> data1 = Map.of("id", 1L, "name", "Test User");
            Map<String, Object> data2 = Map.of("id", 1L, "name", "Test User");

            Object obj1 = factory.createFromMap("Person", data1);
            Object obj2 = factory.createFromMap("Person", data2);

            assertEquals(obj1.hashCode(), obj2.hashCode());
        }

        /*
         * AI GENERATED AND NOT WORKING. MAYBE WRONG TEST LIB VERSION
         * @Test
        @DisplayName("Should throw exception for unsupported methods")
        void testUnsupportedMethods() throws Exception {
            Map<String, Object> data = Map.of("id", 1L, "name", "Test User");
            Object obj = factory.createFromMap("Person", data);

            // Use reflection to call an unsupported method
            assertThrows(UnsupportedOperationException.class, () -> {
                obj.getClass().getMethod("unsupportedMethod").invoke(obj);
            }, UnsupportedOperationException.class);
        }
        */
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null values in data")
        void testNullValuesInData() throws Exception {
            factory.registerObjectDefinition(personDefinition);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", 1L);
            data.put("name", "Test User");
            data.put("email", null);  // Explicit null value

            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", data);

            assertEquals(1L, obj.getFieldValue("id"));
            assertEquals("Test User", obj.getFieldValue("name"));
            assertNull(obj.getFieldValue("email"));
        }

        @Test
        @DisplayName("Should handle empty collections")
        void testEmptyCollections() throws Exception {
            factory.registerObjectDefinition(personDefinition);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", 1L);
            data.put("name", "Test User");
            data.put("scores", Collections.emptyList());
            data.put("metadata", Collections.emptyMap());

            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", data);

            assertEquals(Collections.emptyList(), obj.getFieldValue("scores"));
            assertEquals(Collections.emptyMap(), obj.getFieldValue("metadata"));
        }

        @Test
        @DisplayName("Should handle single value converted to list")
        void testSingleValueToList() throws Exception {
            factory.registerObjectDefinition(personDefinition);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", 1L);
            data.put("name", "Test User");
            data.put("scores", 95);  // Single value that should become a list

            DynamicObjectFactory.DynamicObject obj = 
                (DynamicObjectFactory.DynamicObject) factory.createFromMap("Person", data);

            assertEquals(Arrays.asList(95), obj.getFieldValue("scores"));
        }
    }
}
