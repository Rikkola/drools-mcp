package org.drools;

import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DRLRunner {

    /**
     * Executes a DRL file that may contain declared types and data creation rules
     * @param drlContent The DRL content as a string
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRL(String drlContent) {
        return runDRL(drlContent, 0); // 0 means unlimited rules
    }

    /**
     * Executes a DRL file that may contain declared types and data creation rules
     * @param drlContent The DRL content as a string
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRL(String drlContent, int maxRuns) {
        try {
            // Create KieSession from DRL string
            KieHelper kieHelper = new KieHelper();
            kieHelper.addContent(drlContent, ResourceType.DRL);
            
            // Build and check for errors
            KieContainer kieContainer = kieHelper.getKieContainer();
            if (kieHelper.verify().hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException("DRL compilation errors: " + 
                    kieHelper.verify().getMessages(Message.Level.ERROR));
            }
            
            KieSession kieSession = kieContainer.newKieSession();

            // Fire all rules (including data creation rules)
            int firedRules;
            if (maxRuns > 0) {
                firedRules = kieSession.fireAllRules(maxRuns);
            } else {
                firedRules = kieSession.fireAllRules();
            }
            System.out.println("Fired " + firedRules + " rules");

            // Collect all facts from working memory
            Collection<?> facts = kieSession.getObjects();
            List<Object> factList = new ArrayList<>(facts);
            
            // Print facts for debugging
            System.out.println("Facts in working memory: " + factList.size());
            factList.forEach(fact -> System.out.println("  " + fact));
            
            // Dispose session
            kieSession.dispose();
            
            return factList;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a DRL file with external facts
     * @param drlContent The DRL content as a string
     * @param facts External facts to insert into working memory
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithFacts(String drlContent, List<Object> facts) {
        return runDRLWithFacts(drlContent, facts, 0); // 0 means unlimited rules
    }

    /**
     * Executes a DRL file with external facts
     * @param drlContent The DRL content as a string
     * @param facts External facts to insert into working memory
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return List of facts in working memory after rule execution
     */
    public static List<Object> runDRLWithFacts(String drlContent, List<Object> facts, int maxRuns) {
        try {
            // Create KieSession from DRL string
            KieHelper kieHelper = new KieHelper();
            kieHelper.addContent(drlContent, ResourceType.DRL);
            
            // Build and check for errors
            KieContainer kieContainer = kieHelper.getKieContainer();
            if (kieHelper.verify().hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException("DRL compilation errors: " + 
                    kieHelper.verify().getMessages(Message.Level.ERROR));
            }
            
            KieSession kieSession = kieContainer.newKieSession();

            // Insert external facts
            for (Object fact : facts) {
                kieSession.insert(fact);
                System.out.println("Inserted external fact: " + fact);
            }

            // Fire all rules
            int firedRules;
            if (maxRuns > 0) {
                firedRules = kieSession.fireAllRules(maxRuns);
            } else {
                firedRules = kieSession.fireAllRules();
            }
            System.out.println("Fired " + firedRules + " rules");

            // Collect all facts from working memory
            Collection<?> workingMemoryFacts = kieSession.getObjects();
            List<Object> factList = new ArrayList<>(workingMemoryFacts);
            
            // Print facts for debugging
            System.out.println("Facts in working memory: " + factList.size());
            factList.forEach(fact -> System.out.println("  " + fact));
            
            // Dispose session
            kieSession.dispose();
            
            return factList;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute DRL with facts: " + e.getMessage(), e);
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