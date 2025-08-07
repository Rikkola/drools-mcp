package org.drools.agentic.example.services.execution;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.drools.agentic.example.storage.KnowledgeBaseStorage;

import java.util.List;
import java.util.Map;

/**
 * Non-AI agent that executes rules from shared knowledge base storage.
 * This agent performs pure Java operations without any LLM calls.
 * Uses shared storage for execution-only operations.
 */
public class KnowledgeRunnerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final KnowledgeBaseStorage storage = KnowledgeBaseStorage.getInstance();

    @Tool("STEP 1: Execute rules with facts using the shared knowledge base. Use this to run DRL rules against fact data. Provide facts as JSON array with optional _type field for dynamic object creation.")
    public String executeRules(@P("jsonFacts - JSON array of fact objects to insert into the session, each can have optional '_type' field") String jsonFacts,
                              @P("maxActivations - Maximum number of rule activations, or null for unlimited") Integer maxActivations) {
        // Note: jsonFacts should be a JSON array where each object can optionally include
        // a '_type' field to specify the fact type for dynamic object creation.
        // Without '_type', facts are inserted as Map objects.
        // Example: [{"_type":"Person", "name":"John", "age":25}, {"name":"Jane", "age":30}]
        try {
            StringBuilder response = new StringBuilder();
            response.append("🚀 Knowledge Base Execution\n");
            response.append("=".repeat(28) + "\n\n");

            // Check if knowledge base is available
            if (!storage.hasKnowledgeBase() || !storage.hasSession()) {
                response.append("❌ No knowledge base available for execution.\n");
                response.append("Please use DroolsKnowledgeBaseService to build a knowledge base first.\n");
                return response.toString();
            }
            
            KieSession kieSession = storage.getSession();
            KnowledgeBaseStorage.KnowledgeBaseInfo info = storage.getInfo();
            
            response.append("📋 Using Knowledge Base: ").append(info.name()).append("\n");
            response.append("📋 Session ID: ").append(info.sessionId()).append("\n\n");
            
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
            
            response.append("\n✅ Execution completed successfully!\n");
            response.append("Session remains active in shared storage for further operations.\n");
            
            return response.toString();
            
        } catch (Exception e) {
            return "❌ Execution failed: " + e.getMessage();
        }
    }

    @Tool("STEP 2: Get status and info about the shared knowledge base. Use this to check if a knowledge base is available and get details about its current state.")
    public String getKnowledgeBaseStatus() {
        StringBuilder response = new StringBuilder();
        response.append("📚 Shared Knowledge Base Status\n");
        response.append("=".repeat(32) + "\n\n");
        
        if (!storage.hasKnowledgeBase()) {
            response.append("❌ No knowledge base available in shared storage.\n");
            response.append("Please use DroolsKnowledgeBaseService to build a knowledge base first.\n");
            return response.toString();
        }
        
        KnowledgeBaseStorage.KnowledgeBaseInfo info = storage.getInfo();
        response.append("📋 Knowledge Base Details:\n");
        response.append("  • Name: ").append(info.name()).append("\n");
        response.append("  • Release ID: ").append(info.releaseId()).append("\n");
        response.append("  • Session Active: ").append(info.sessionActive() ? "Yes" : "No").append("\n");
        response.append("  • Session ID: ").append(info.sessionId()).append("\n");
        response.append("  • Facts in Memory: ").append(info.factCount()).append("\n");
        response.append("  • Created: ").append(info.createdTime()).append("\n");
        response.append("  • Source: ").append(info.sourceInfo()).append("\n");
        
        response.append("\n✅ Knowledge base is ready for execution!\n");
        
        return response.toString();
    }

    @Tool("STEP 3: Clear all facts from the shared session. Use this to remove all facts from the current session while keeping the session active.")
    public String clearFacts() {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🧹 Clearing Facts from Session\n");
            response.append("=".repeat(31) + "\n\n");
            
            if (!storage.hasSession()) {
                response.append("❌ No active session found in shared storage.\n");
                return response.toString();
            }
            
            long clearedCount = storage.clearFacts();
            
            response.append("✅ Cleared ").append(clearedCount).append(" facts from the session.\n");
            response.append("Session remains active and ready for new fact insertions.\n");
            
            return response.toString();
            
        } catch (Exception e) {
            return "❌ Failed to clear facts: " + e.getMessage();
        }
    }

    @Tool("STEP 4: Execute rules multiple times with different fact sets. Use this for batch processing where you want to test multiple scenarios sequentially.")
    public String executeBatch(@P("jsonFactBatches - JSON array of fact batch arrays for sequential execution") String jsonFactBatches,
                              @P("maxActivations - Maximum number of rule activations per batch, or null for unlimited") Integer maxActivations) {
        // Note: jsonFactBatches should be a JSON array of fact arrays, where each fact
        // can optionally include a '_type' field for dynamic object creation.
        // Example: [[[{"_type":"Person", "name":"John"}], [{"_type":"Order", "amount":100}]]]
        try {
            StringBuilder response = new StringBuilder();
            response.append("🚀 Batch Rule Execution\n");
            response.append("=".repeat(23) + "\n\n");
            
            if (!storage.hasKnowledgeBase() || !storage.hasSession()) {
                response.append("❌ No knowledge base available for execution.\n");
                response.append("Please use DroolsKnowledgeBaseService to build a knowledge base first.\n");
                return response.toString();
            }
            
            // Parse batch of fact sets
            List<List<Map<String, Object>>> factBatches = objectMapper.readValue(jsonFactBatches, 
                new TypeReference<List<List<Map<String, Object>>>>() {});
            
            KieSession kieSession = storage.getSession();
            KnowledgeBaseStorage.KnowledgeBaseInfo info = storage.getInfo();
            
            response.append("📋 Using Knowledge Base: ").append(info.name()).append("\n");
            response.append("📋 Processing ").append(factBatches.size()).append(" fact batches\n\n");
            
            int batchNumber = 1;
            int totalRulesFired = 0;
            
            for (List<Map<String, Object>> facts : factBatches) {
                response.append("🔄 Batch ").append(batchNumber).append(":\n");
                
                // Clear previous facts
                storage.clearFacts();
                
                // Insert new facts
                for (Map<String, Object> fact : facts) {
                    kieSession.insert(fact);
                }
                
                // Fire rules
                int rulesCount;
                if (maxActivations != null && maxActivations > 0) {
                    rulesCount = kieSession.fireAllRules(maxActivations);
                } else {
                    rulesCount = kieSession.fireAllRules();
                }
                
                response.append("  • Facts: ").append(facts.size()).append("\n");
                response.append("  • Rules Fired: ").append(rulesCount).append("\n\n");
                
                totalRulesFired += rulesCount;
                batchNumber++;
            }
            
            response.append("✅ Batch execution completed successfully!\n");
            response.append("Total rules fired across all batches: ").append(totalRulesFired).append("\n");
            
            return response.toString();
            
        } catch (Exception e) {
            return "❌ Batch execution failed: " + e.getMessage();
        }
    }
}