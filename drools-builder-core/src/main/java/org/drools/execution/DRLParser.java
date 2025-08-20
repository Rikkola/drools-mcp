package org.drools.execution;

/**
 * Utility class for parsing and analyzing DRL content
 */
public class DRLParser {

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

}