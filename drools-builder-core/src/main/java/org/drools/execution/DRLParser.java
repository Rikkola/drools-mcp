package org.drools.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and analyzing DRL content
 */
public class DRLParser {

    private static final Pattern DECLARE_PATTERN = Pattern.compile(
        "declare\\s+([A-Za-z_][A-Za-z0-9_]*)\\s+[\\s\\S]*?\\s+end",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    /**
     * Extracts the package name from DRL content
     * @param drlContent The DRL content to parse
     * @return Package name or empty string if not found
     */
    public String extractPackageName(String drlContent) {
        if (drlContent == null) {
            return "";
        }
        
        String[] lines = drlContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("package ")) {
                String packageName = line.substring(8).trim();
                if (packageName.endsWith(";")) {
                    packageName = packageName.substring(0, packageName.length() - 1);
                }
                return packageName.trim();
            }
        }
        return "";
    }

    /**
     * Extracts the first declared type name from DRL content
     * @param drlContent The DRL content to parse
     * @return First declared type name or null if not found
     */
    public String extractDeclaredTypeName(String drlContent) {
        if (drlContent == null) {
            return null;
        }
        
        String[] lines = drlContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("declare ")) {
                String[] parts = line.split("\\s+");
                if (parts.length > 1) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    /**
     * Extracts all declared type names from DRL content
     * @param drlContent The DRL content to parse
     * @return List of all declared type names (empty list if none found)
     */
    public List<String> extractAllDeclaredTypes(String drlContent) {
        List<String> typeNames = new ArrayList<>();
        
        if (drlContent == null) {
            return typeNames;
        }

        Matcher matcher = DECLARE_PATTERN.matcher(drlContent);
        while (matcher.find()) {
            String typeName = matcher.group(1);
            if (typeName != null && !typeName.trim().isEmpty()) {
                typeNames.add(typeName.trim());
            }
        }
        
        return typeNames;
    }

    /**
     * Extracts complete declare statements from DRL content
     * @param drlContent The DRL content to parse
     * @return List of complete declare statements with their type names
     */
    public List<DeclareStatement> extractDeclareStatements(String drlContent) {
        List<DeclareStatement> statements = new ArrayList<>();
        
        if (drlContent == null) {
            return statements;
        }

        Matcher matcher = DECLARE_PATTERN.matcher(drlContent);
        while (matcher.find()) {
            String fullStatement = matcher.group(0).trim();
            String typeName = matcher.group(1).trim();
            statements.add(new DeclareStatement(typeName, fullStatement));
        }
        
        return statements;
    }

    /**
     * Checks if DRL content contains any declared types
     * @param drlContent The DRL content to check
     * @return true if DRL contains declared types, false otherwise
     */
    public boolean hasDeclaredTypes(String drlContent) {
        return !extractAllDeclaredTypes(drlContent).isEmpty();
    }

    /**
     * Checks if DRL content contains a specific declared type
     * @param drlContent The DRL content to check
     * @param typeName The type name to look for
     * @return true if the specified type is declared, false otherwise
     */
    public boolean hasDeclaredType(String drlContent, String typeName) {
        List<String> types = extractAllDeclaredTypes(drlContent);
        return types.contains(typeName);
    }

    /**
     * Validates basic DRL syntax (very basic validation)
     * @param drlContent The DRL content to validate
     * @return true if basic syntax appears valid, false otherwise
     */
    public boolean isValidBasicSyntax(String drlContent) {
        if (drlContent == null || drlContent.trim().isEmpty()) {
            return false;
        }

        // Check for balanced braces in declare statements
        List<DeclareStatement> statements = extractDeclareStatements(drlContent);
        for (DeclareStatement statement : statements) {
            if (!statement.getFullStatement().contains("end")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Represents a declare statement with its type name and full content
     */
    public static class DeclareStatement {
        private final String typeName;
        private final String fullStatement;

        public DeclareStatement(String typeName, String fullStatement) {
            this.typeName = typeName;
            this.fullStatement = fullStatement;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getFullStatement() {
            return fullStatement;
        }

        @Override
        public String toString() {
            return "DeclareStatement{" +
                    "typeName='" + typeName + '\'' +
                    ", fullStatement='" + fullStatement + '\'' +
                    '}';
        }
    }
}