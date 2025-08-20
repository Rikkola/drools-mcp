package org.drools.execution;

import org.kie.api.runtime.KieContainer;

import java.util.Collections;
import java.util.List;

/**
 * Simplified facade for DRL execution with various input types.
 * This class delegates to specialized components for actual work.
 */
public class DRLPopulatorRunner {

    private static final DRLExecutor executor = new DRLExecutor();
    private static final FactBuilder factBuilder = new FactBuilder();
    private static final DRLParser parser = new DRLParser();

    /**
     * Executes a DRL file that may contain declared types and data creation rules
     * @param drlContent The DRL content as a string
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRL(String drlContent) {
        return executor.execute(drlContent, Collections.emptyList(), 0);
    }

    /**
     * Executes a DRL file that may contain declared types and data creation rules
     * @param drlContent The DRL content as a string
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRL(String drlContent, int maxRuns) {
        return executor.execute(drlContent, Collections.emptyList(), maxRuns);
    }

    /**
     * Executes a DRL file with external facts provided as JSON
     * @param drlContent The DRL content as a string
     * @param factsJson JSON string containing array of facts with type fields
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRLWithJsonFacts(String drlContent, String factsJson) {
        return runDRLWithJsonFacts(drlContent, factsJson, 0);
    }

    /**
     * Executes a DRL file with external facts provided as JSON
     * @param drlContent The DRL content as a string
     * @param factsJson JSON string containing array of facts with type fields
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRLWithJsonFacts(String drlContent, String factsJson, int maxRuns) {
        try {
            // Build KieContainer once for both fact creation and execution
            KieContainer kieContainer = executor.buildKieContainer(drlContent);
            
            // Extract package name
            String packageName = parser.extractPackageName(drlContent);
            
            // Build facts from JSON
            List<Object> facts = factBuilder.buildFromJsonArray(factsJson, kieContainer, packageName);
            
            // Execute with facts using pre-built container
            return executor.executeWithContainer(kieContainer, facts, maxRuns);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL with JSON facts: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a DRL file with external facts
     * @param drlContent The DRL content as a string
     * @param facts External facts to insert into working memory
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRLWithFacts(String drlContent, List<Object> facts) {
        return executor.execute(drlContent, facts, 0);
    }

    /**
     * Executes a DRL file with external facts
     * @param drlContent The DRL content as a string
     * @param facts External facts to insert into working memory
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRLWithFacts(String drlContent, List<Object> facts, int maxRuns) {
        return executor.execute(drlContent, facts, maxRuns);
    }

    /**
     * Executes a DRL file and populates it with JSON data
     * @param drlContent The DRL content as a string
     * @param jsonString JSON data to populate declared types
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRL(String drlContent, String jsonString) {
        return runDRL(drlContent, jsonString, 0);
    }

    /**
     * Executes a DRL file and populates it with JSON data
     * @param drlContent The DRL content as a string
     * @param jsonString JSON data to populate declared types
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRL(String drlContent, String jsonString, int maxRuns) {
        try {
            // Build KieContainer once
            KieContainer kieContainer = executor.buildKieContainer(drlContent);
            
            // Extract package name
            String packageName = parser.extractPackageName(drlContent);
            
            // Build single fact from JSON
            Object fact = factBuilder.buildFromJsonSingle(jsonString, kieContainer, packageName);
            
            // Execute with the single fact
            List<Object> facts = fact != null ? List.of(fact) : Collections.emptyList();
            return executor.executeWithContainer(kieContainer, facts, maxRuns);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL with JSON population: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a DRL file and populates it with JSON data using specified package and type names
     * @param drlContent The DRL content as a string
     * @param jsonString JSON data to populate declared types
     * @param packageName Package name for the declared type
     * @param declaredTypeName Name of the declared type to populate
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRL(String drlContent, String jsonString, String packageName, String declaredTypeName) {
        return runDRL(drlContent, jsonString, packageName, declaredTypeName, 0);
    }

    /**
     * Executes a DRL file and populates it with JSON data using specified package and type names
     * @param drlContent The DRL content as a string
     * @param jsonString JSON data to populate declared types
     * @param packageName Package name for the declared type
     * @param declaredTypeName Name of the declared type to populate
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return DRLRunnerResult containing facts in working memory and fired rules count after rule execution
     */
    public static DRLRunnerResult runDRL(String drlContent, String jsonString, String packageName, String declaredTypeName, int maxRuns) {
        try {
            // Build KieContainer once
            KieContainer kieContainer = executor.buildKieContainer(drlContent);
            
            // Build fact with explicit type information
            Object fact = factBuilder.buildFromJsonWithExplicitType(jsonString, kieContainer, packageName, declaredTypeName);
            
            // Execute with the fact
            List<Object> facts = List.of(fact);
            return executor.executeWithContainer(kieContainer, facts, maxRuns);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL with JSON population: " + e.getMessage(), e);
        }
    }

    /**
     * Filter facts by type name (useful for declared types)
     * @param facts List of facts
     * @param typeName Name of the type to filter by
     * @return List of facts matching the type name
     */
    public static List<Object> filterFactsByType(List<Object> facts, String typeName) {
        return facts.stream()
                .filter(fact -> fact.getClass().getSimpleName().equals(typeName))
                .collect(java.util.stream.Collectors.toList());
    }

}