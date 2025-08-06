package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * Non-AI agent that builds knowledge bases and executes rules deterministically.
 * This agent performs pure Java operations without any LLM calls.
 */
public class KnowledgeRunnerAgent {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Agent(description = "Build a knowledge base from DRL content and execute rules with facts", 
           outputName = "executionResult")
    public String buildAndExecute(@V("drlContent") String drlContent,
                                 @V("facts") String jsonFacts,
                                 @V("maxActivations") Integer maxActivations) {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🚀 Knowledge Base Build and Execution\n");
            response.append("=".repeat(40) + "\n\n");

            // Build knowledge base
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            kfs.write("src/main/resources/rules.drl", drlContent);
            
            KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
            
            // Check for build errors
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                response.append("❌ Knowledge Base Build Failed:\n");
                for (Message message : kieBuilder.getResults().getMessages(Message.Level.ERROR)) {
                    response.append("• ").append(message.getText()).append("\n");
                }
                return response.toString();
            }
            
            // Create session and execute
            KieContainer kieContainer = ks.newKieContainer(kieBuilder.getKieModule().getReleaseId());
            KieSession kieSession = kieContainer.newKieSession();
            
            response.append("✅ Knowledge Base Built Successfully!\n\n");
            
            // Parse and insert facts
            List<Map<String, Object>> facts = objectMapper.readValue(jsonFacts, 
                new TypeReference<List<Map<String, Object>>>() {});
            
            response.append("📊 Inserting Facts:\n");
            for (Map<String, Object> fact : facts) {
                kieSession.insert(fact);
                response.append("  • ").append(fact).append("\n");
            }
            
            // Execute rules
            response.append("\n🔥 Executing Rules:\n");
            int rulesCount;
            if (maxActivations != null && maxActivations > 0) {
                rulesCount = kieSession.fireAllRules(maxActivations);
            } else {
                rulesCount = kieSession.fireAllRules();
            }
            
            response.append("  • Rules Fired: ").append(rulesCount).append("\n");
            response.append("  • Facts in Memory: ").append(kieSession.getFactCount()).append("\n");
            
            // Cleanup
            kieSession.dispose();
            
            response.append("\n✅ Execution completed successfully!\n");
            
            return response.toString();
            
        } catch (Exception e) {
            return "❌ Execution failed: " + e.getMessage();
        }
    }

    @Agent(description = "Validate DRL syntax without execution", 
           outputName = "validationResult")
    public String validateDRL(@V("drlContent") String drlContent) {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🔍 DRL Validation\n");
            response.append("=".repeat(17) + "\n\n");
            
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            kfs.write("src/main/resources/rules.drl", drlContent);
            
            KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
            
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                response.append("❌ Validation Failed:\n");
                for (Message message : kieBuilder.getResults().getMessages(Message.Level.ERROR)) {
                    response.append("• ").append(message.getText()).append("\n");
                }
            } else {
                response.append("✅ Validation Passed!\n");
                response.append("DRL syntax is valid and ready for execution.\n");
            }
            
            // Show warnings if any
            if (kieBuilder.getResults().hasMessages(Message.Level.WARNING)) {
                response.append("\n⚠️ Warnings:\n");
                for (Message message : kieBuilder.getResults().getMessages(Message.Level.WARNING)) {
                    response.append("• ").append(message.getText()).append("\n");
                }
            }
            
            return response.toString();
            
        } catch (Exception e) {
            return "❌ Validation error: " + e.getMessage();
        }
    }

    @Agent(description = "Count DRL elements in content", 
           outputName = "elementCounts")
    public String analyzeDRL(@V("drlContent") String drlContent) {
        StringBuilder response = new StringBuilder();
        response.append("📊 DRL Content Analysis\n");
        response.append("=".repeat(24) + "\n\n");
        
        long ruleCount = drlContent.lines()
            .filter(line -> line.trim().startsWith("rule "))
            .count();
        long declareCount = drlContent.lines()
            .filter(line -> line.trim().startsWith("declare "))
            .count();
        long globalCount = drlContent.lines()
            .filter(line -> line.trim().startsWith("global "))
            .count();
        long functionCount = drlContent.lines()
            .filter(line -> line.trim().startsWith("function "))
            .count();
        long queryCount = drlContent.lines()
            .filter(line -> line.trim().startsWith("query "))
            .count();
        
        response.append("📋 Element Counts:\n");
        response.append("  • Rules: ").append(ruleCount).append("\n");
        response.append("  • Declared Types: ").append(declareCount).append("\n");
        response.append("  • Globals: ").append(globalCount).append("\n");
        response.append("  • Functions: ").append(functionCount).append("\n");
        response.append("  • Queries: ").append(queryCount).append("\n");
        
        long totalLines = drlContent.lines().count();
        long nonEmptyLines = drlContent.lines()
            .filter(line -> !line.trim().isEmpty())
            .count();
        
        response.append("\n📝 Content Stats:\n");
        response.append("  • Total Lines: ").append(totalLines).append("\n");
        response.append("  • Non-empty Lines: ").append(nonEmptyLines).append("\n");
        
        return response.toString();
    }

    @Agent(description = "Format facts for rule execution", 
           outputName = "formattedFacts")
    public String formatFacts(@V("rawFacts") String rawFacts) {
        try {
            // Parse the input facts
            List<Map<String, Object>> facts = objectMapper.readValue(rawFacts, 
                new TypeReference<List<Map<String, Object>>>() {});
            
            StringBuilder response = new StringBuilder();
            response.append("🔧 Facts Formatting\n");
            response.append("=".repeat(19) + "\n\n");
            
            response.append("📊 Formatted Facts:\n");
            for (int i = 0; i < facts.size(); i++) {
                Map<String, Object> fact = facts.get(i);
                response.append("  ").append(i + 1).append(". ").append(fact).append("\n");
            }
            
            response.append("\nTotal facts: ").append(facts.size()).append("\n");
            response.append("✅ Facts formatted successfully for rule execution.\n");
            
            return response.toString();
            
        } catch (Exception e) {
            return "❌ Facts formatting failed: " + e.getMessage();
        }
    }
}