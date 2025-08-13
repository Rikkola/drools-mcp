package org.drools.execution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Test to debug the declare statement extraction in DRLRunner
 */
public class DeclareExtractionTest {

    @Test
    @DisplayName("Debug declare statement extraction")
    public void debugDeclareExtraction() {
        String drlContent = """
            declare User
                age : int
                adult : boolean
            end
            """;
        
        System.out.println("=== Original DRL Content ===");
        System.out.println("'" + drlContent + "'");
        
        // Use the same pattern as DRLRunner (new fixed version)
        Pattern declarePattern = Pattern.compile(
            "(declare\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+[\\s\\S]*?\\s+end)", 
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );
        
        Matcher matcher = declarePattern.matcher(drlContent);
        
        while (matcher.find()) {
            String fullDeclareStatement = matcher.group(1).trim();
            String typeName = matcher.group(2).trim();
            
            System.out.println("=== Extracted Parts ===");
            System.out.println("Type name: '" + typeName + "'");
            System.out.println("Full statement: '" + fullDeclareStatement + "'");
            System.out.println("Full statement: '" + fullDeclareStatement + "'");
            
            // Test what gets registered in DefinitionStorage
            org.drools.storage.DefinitionStorage storage = new org.drools.storage.DefinitionStorage();
            storage.addDefinition(typeName, "declare", fullDeclareStatement);
            
            // Test what DynamicJsonToJavaFactory does with this
            DynamicJsonToJavaFactory factory = new DynamicJsonToJavaFactory(storage);
            
            try {
                java.util.List<Object> objects = factory.createObjectsFromJson("{\"age\": 25, \"adult\": false}", typeName);
                System.out.println("✅ Success: " + objects);
            } catch (Exception e) {
                System.out.println("❌ Failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}