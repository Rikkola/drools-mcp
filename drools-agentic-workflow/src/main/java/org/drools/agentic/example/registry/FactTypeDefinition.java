package org.drools.agentic.example.registry;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a DRL fact type declaration with its fields and metadata.
 */
public class FactTypeDefinition {
    private String typeName;
    private Map<String, FieldDefinition> fields;
    private String packageName;
    private LocalDateTime lastModified;

    public FactTypeDefinition() {
        this.fields = new HashMap<>();
        this.lastModified = LocalDateTime.now();
    }

    public FactTypeDefinition(String typeName, String packageName) {
        this();
        this.typeName = typeName;
        this.packageName = packageName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
        this.lastModified = LocalDateTime.now();
    }

    public Map<String, FieldDefinition> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldDefinition> fields) {
        this.fields = fields != null ? fields : new HashMap<>();
        this.lastModified = LocalDateTime.now();
    }

    public void addField(String name, String type) {
        addField(name, type, null, false);
    }

    public void addField(String name, String type, String defaultValue) {
        addField(name, type, defaultValue, false);
    }

    public void addField(String name, String type, String defaultValue, boolean required) {
        this.fields.put(name, new FieldDefinition(name, type, defaultValue, required));
        this.lastModified = LocalDateTime.now();
    }

    public void removeField(String name) {
        this.fields.remove(name);
        this.lastModified = LocalDateTime.now();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        this.lastModified = LocalDateTime.now();
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Generates the DRL declare block for this fact type.
     */
    public String generateDRLDeclaration() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare ").append(typeName).append("\n");
        
        for (FieldDefinition field : fields.values()) {
            sb.append("    ").append(field.getName()).append(" : ").append(field.getType());
            if (field.getDefaultValue() != null && !field.getDefaultValue().trim().isEmpty()) {
                sb.append(" = ").append(field.getDefaultValue());
            }
            sb.append("\n");
        }
        
        sb.append("end");
        return sb.toString();
    }

    /**
     * Checks if this fact type is compatible with another (same name and field types).
     */
    public boolean isCompatibleWith(FactTypeDefinition other) {
        if (!Objects.equals(this.typeName, other.typeName)) {
            return false;
        }
        
        for (Map.Entry<String, FieldDefinition> entry : this.fields.entrySet()) {
            FieldDefinition otherField = other.fields.get(entry.getKey());
            if (otherField != null && !entry.getValue().getType().equals(otherField.getType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merges fields from another fact type definition, adding new fields.
     */
    public void mergeFields(FactTypeDefinition other) {
        for (Map.Entry<String, FieldDefinition> entry : other.fields.entrySet()) {
            if (!this.fields.containsKey(entry.getKey())) {
                this.fields.put(entry.getKey(), entry.getValue().copy());
            }
        }
        this.lastModified = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactTypeDefinition that = (FactTypeDefinition) o;
        return Objects.equals(typeName, that.typeName) && 
               Objects.equals(fields, that.fields) && 
               Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, fields, packageName);
    }

    @Override
    public String toString() {
        return "FactTypeDefinition{" +
                "typeName='" + typeName + '\'' +
                ", fields=" + fields.size() +
                ", packageName='" + packageName + '\'' +
                ", lastModified=" + lastModified +
                '}';
    }

    /**
     * Represents a field in a fact type declaration.
     */
    public static class FieldDefinition {
        private String name;
        private String type;
        private String defaultValue;
        private boolean required;

        public FieldDefinition() {}

        public FieldDefinition(String name, String type) {
            this(name, type, null, false);
        }

        public FieldDefinition(String name, String type, String defaultValue, boolean required) {
            this.name = name;
            this.type = type;
            this.defaultValue = defaultValue;
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public FieldDefinition copy() {
            return new FieldDefinition(name, type, defaultValue, required);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldDefinition that = (FieldDefinition) o;
            return required == that.required && 
                   Objects.equals(name, that.name) && 
                   Objects.equals(type, that.type) && 
                   Objects.equals(defaultValue, that.defaultValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, defaultValue, required);
        }

        @Override
        public String toString() {
            return name + " : " + type + (defaultValue != null ? " = " + defaultValue : "");
        }
    }
}