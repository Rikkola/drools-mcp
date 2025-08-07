package org.drools.agentic.example.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the FactTypeRegistry.
 * Thread-safe for concurrent access.
 */
public class InMemoryFactTypeRegistry implements FactTypeRegistry {
    
    private final Map<String, FactTypeDefinition> factTypes = new ConcurrentHashMap<>();
    
    // Pattern for parsing DRL declare blocks
    private static final Pattern DECLARE_PATTERN = Pattern.compile(
        "declare\\s+(\\w+)\\s*(.*?)\\s*end", 
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );
    
    // Pattern for parsing field definitions within declare blocks
    private static final Pattern FIELD_PATTERN = Pattern.compile(
        "^\\s*(\\w+)\\s*:\\s*(\\S+)(?:\\s*=\\s*(.+))?\\s*$",
        Pattern.MULTILINE
    );

    @Override
    public void registerFactType(String typeName, FactTypeDefinition definition) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Type name cannot be null or empty");
        }
        if (definition == null) {
            throw new IllegalArgumentException("Fact type definition cannot be null");
        }
        
        definition.setTypeName(typeName);
        factTypes.put(typeName, definition);
    }

    @Override
    public Optional<FactTypeDefinition> getFactType(String typeName) {
        if (typeName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(factTypes.get(typeName));
    }

    @Override
    public Set<String> getAllFactTypeNames() {
        return new HashSet<>(factTypes.keySet());
    }

    @Override
    public List<FactTypeDefinition> getAllFactTypes() {
        return new ArrayList<>(factTypes.values());
    }

    @Override
    public void updateFactType(String typeName, FactTypeDefinition definition) {
        if (typeName == null || typeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Type name cannot be null or empty");
        }
        if (definition == null) {
            throw new IllegalArgumentException("Fact type definition cannot be null");
        }
        
        FactTypeDefinition existing = factTypes.get(typeName);
        if (existing != null && existing.isCompatibleWith(definition)) {
            // Merge fields if compatible
            existing.mergeFields(definition);
        } else {
            // Replace with new definition
            definition.setTypeName(typeName);
            factTypes.put(typeName, definition);
        }
    }

    @Override
    public boolean removeFactType(String typeName) {
        if (typeName == null) {
            return false;
        }
        return factTypes.remove(typeName) != null;
    }

    @Override
    public String generateDRLDeclarations() {
        return factTypes.values().stream()
                .map(FactTypeDefinition::generateDRLDeclaration)
                .collect(Collectors.joining("\n\n"));
    }

    @Override
    public String generateDRLDeclarations(Set<String> typeNames) {
        if (typeNames == null || typeNames.isEmpty()) {
            return "";
        }
        
        return typeNames.stream()
                .map(factTypes::get)
                .filter(Objects::nonNull)
                .map(FactTypeDefinition::generateDRLDeclaration)
                .collect(Collectors.joining("\n\n"));
    }

    @Override
    public boolean hasFactType(String typeName) {
        return typeName != null && factTypes.containsKey(typeName);
    }

    @Override
    public void clear() {
        factTypes.clear();
    }

    @Override
    public int size() {
        return factTypes.size();
    }

    @Override
    public int loadFromDRL(String drlContent, String packageName) {
        if (drlContent == null || drlContent.trim().isEmpty()) {
            return 0;
        }
        
        int loadedCount = 0;
        Matcher declareMatcher = DECLARE_PATTERN.matcher(drlContent);
        
        while (declareMatcher.find()) {
            String typeName = declareMatcher.group(1);
            String fieldsSection = declareMatcher.group(2);
            
            FactTypeDefinition definition = new FactTypeDefinition(typeName, packageName);
            
            // Parse fields
            Matcher fieldMatcher = FIELD_PATTERN.matcher(fieldsSection);
            while (fieldMatcher.find()) {
                String fieldName = fieldMatcher.group(1);
                String fieldType = fieldMatcher.group(2);
                String defaultValue = fieldMatcher.group(3);
                
                if (defaultValue != null) {
                    defaultValue = defaultValue.trim();
                    // Remove quotes if present
                    if (defaultValue.startsWith("\"") && defaultValue.endsWith("\"")) {
                        defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                    }
                }
                
                definition.addField(fieldName, fieldType, defaultValue);
            }
            
            registerFactType(typeName, definition);
            loadedCount++;
        }
        
        return loadedCount;
    }

    @Override
    public void merge(FactTypeRegistry other) {
        if (other == null) {
            return;
        }
        
        for (String typeName : other.getAllFactTypeNames()) {
            Optional<FactTypeDefinition> otherDefinition = other.getFactType(typeName);
            if (otherDefinition.isPresent()) {
                updateFactType(typeName, otherDefinition.get());
            }
        }
    }

    @Override
    public FactTypeRegistry copy() {
        InMemoryFactTypeRegistry copy = new InMemoryFactTypeRegistry();
        
        for (Map.Entry<String, FactTypeDefinition> entry : factTypes.entrySet()) {
            FactTypeDefinition originalDef = entry.getValue();
            FactTypeDefinition copiedDef = new FactTypeDefinition(
                originalDef.getTypeName(), 
                originalDef.getPackageName()
            );
            
            // Copy fields
            for (Map.Entry<String, FactTypeDefinition.FieldDefinition> fieldEntry : 
                 originalDef.getFields().entrySet()) {
                FactTypeDefinition.FieldDefinition field = fieldEntry.getValue();
                copiedDef.addField(field.getName(), field.getType(), 
                                 field.getDefaultValue(), field.isRequired());
            }
            
            copiedDef.setLastModified(originalDef.getLastModified());
            copy.registerFactType(entry.getKey(), copiedDef);
        }
        
        return copy;
    }


    @Override
    public String toString() {
        return "InMemoryFactTypeRegistry{" +
                "factTypes=" + factTypes.size() +
                ", typeNames=" + factTypes.keySet() +
                '}';
    }
}