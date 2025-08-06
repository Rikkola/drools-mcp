package org.drools.agentic.example.agents;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Drools knowledge base service that builds and manages Drools knowledge bases from DRL files.
 * Can read DRL files from storage and build executable knowledge bases.
 */
public class DroolsKnowledgeBaseService {
    
    private final ChatModel chatModel;
    private final Path storageRoot;
    
    // Single knowledge base and session for this application
    private KieContainer currentKnowledgeBase;
    private KieSession currentSession;
    private String knowledgeBaseName;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public DroolsKnowledgeBaseService(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.storageRoot = Paths.get(System.getProperty("user.home"), ".drools-agent-storage");
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + storageRoot, e);
        }
    }
    
    @Tool("Build a Drools knowledge base from a DRL file")
    public String buildKnowledgeBaseFromFile(@P("The DRL filename (relative to storage root)") String filename) {
        try {
            Path filePath = storageRoot.resolve(filename);
            if (!Files.exists(filePath)) {
                return "❌ DRL file not found: " + filename;
            }
            
            String drlContent = Files.readString(filePath);
            return buildKnowledgeBaseFromContent(drlContent, filename);
            
        } catch (IOException e) {
            return "❌ Error reading DRL file " + filename + ": " + e.getMessage();
        }
    }
    
    @Tool("Build and store a Drools knowledge base from DRL content")
    public String buildKnowledgeBaseFromContent(
            @P("The DRL content to build") String drlContent,
            @P("Name for the knowledge base (will be used as identifier)") String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                name = "main-kb";
            }
            
            // Dispose existing session if any
            if (currentSession != null) {
                currentSession.dispose();
                currentSession = null;
            }
            
            StringBuilder response = new StringBuilder();
            response.append("🏗️ Building and Storing Drools Knowledge Base: ").append(name).append("\n");
            response.append("=".repeat(55 + name.length()) + "\n\n");
            
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            
            // Add the DRL content to the file system
            String resourceName = "src/main/resources/" + name + ".drl";
            kfs.write(resourceName, drlContent);
            
            // Build the knowledge base
            KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
            
            // Check for compilation errors
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                response.append("❌ Knowledge Base Build Failed:\n");
                response.append("-".repeat(30) + "\n");
                for (Message message : kieBuilder.getResults().getMessages(Message.Level.ERROR)) {
                    response.append("• ").append(message.getText()).append("\n");
                }
                return response.toString();
            }
            
            // Create container and store as current
            currentKnowledgeBase = ks.newKieContainer(kieBuilder.getKieModule().getReleaseId());
            currentSession = currentKnowledgeBase.newKieSession();
            knowledgeBaseName = name;
            
            response.append("✅ Knowledge Base Built and Stored Successfully!\n");
            response.append("-".repeat(45) + "\n");
            response.append("📋 Knowledge Base Details:\n");
            response.append("  • Name: ").append(name).append("\n");
            response.append("  • Release ID: ").append(kieBuilder.getKieModule().getReleaseId()).append("\n");
            response.append("  • Resource: ").append(resourceName).append("\n");
            response.append("  • Session Created: Yes\n");
            response.append("  • Session ID: ").append(currentSession.getId()).append("\n");
            
            // Show any warnings
            if (kieBuilder.getResults().hasMessages(Message.Level.WARNING)) {
                response.append("\n⚠️ Warnings:\n");
                for (Message message : kieBuilder.getResults().getMessages(Message.Level.WARNING)) {
                    response.append("• ").append(message.getText()).append("\n");
                }
            }
            
            // Show DRL content summary
            response.append("\n📜 DRL Content Summary:\n");
            response.append("-".repeat(22) + "\n");
            
            long ruleCount = countRules(drlContent);
            long declareCount = countDeclarations(drlContent);
            long globalCount = countGlobals(drlContent);
                
            response.append("  • Rules: ").append(ruleCount).append("\n");
            response.append("  • Declared Types: ").append(declareCount).append("\n");
            response.append("  • Globals: ").append(globalCount).append("\n");
            
            response.append("\n🎯 Knowledge base '").append(name).append("' is now ready for execution!\n");
            response.append("Use 'executeRules' to run rules with JSON facts on-demand.\n");
            
            return response.toString();
            
        } catch (Exception e) {
            return String.format("❌ Knowledge base build failed: %s\n\nPlease check your DRL syntax.", e.getMessage());
        }
    }
   
    // ========== SINGLE SESSION MANAGEMENT ==========
    
    @Tool("Get current knowledge base status")
    public String getKnowledgeBaseStatus() {
        StringBuilder response = new StringBuilder();
        response.append("📚 Current Knowledge Base Status:\n");
        response.append("=".repeat(33) + "\n");
        
        if (currentKnowledgeBase == null) {
            response.append("No knowledge base currently loaded.\n");
            response.append("Use 'buildKnowledgeBaseFromFile' or 'buildKnowledgeBaseFromContent' to create one.\n");
            return response.toString();
        }
        
        response.append("📋 Knowledge Base Details:\n");
        response.append("  • Name: ").append(knowledgeBaseName != null ? knowledgeBaseName : "Unknown").append("\n");
        response.append("  • Release ID: ").append(currentKnowledgeBase.getReleaseId()).append("\n");
        response.append("  • Session Active: ").append(currentSession != null ? "Yes" : "No").append("\n");
        
        if (currentSession != null) {
            response.append("  • Session ID: ").append(currentSession.getId()).append("\n");
            response.append("  • Facts in Memory: ").append(currentSession.getFactCount()).append("\n");
        }
        
        response.append("\n🎯 Ready for rule execution!\n");
        
        return response.toString();
    }
    
    @Tool("Execute rules with JSON facts")
    public String executeRules(
            @P("JSON facts to insert (array format)") String jsonFacts,
            @P("Maximum rule activations (0 for unlimited)") int maxActivations) {
        try {
            if (currentSession == null) {
                return "❌ No active session found. Please build a knowledge base first using 'buildKnowledgeBaseFromFile' or 'buildKnowledgeBaseFromContent'.";
            }
            
            StringBuilder response = new StringBuilder();
            response.append("⚡ Executing Rules:\n");
            response.append("=".repeat(18) + "\n\n");
            
            // Parse and insert facts
            List<Map<String, Object>> facts = objectMapper.readValue(jsonFacts, new TypeReference<List<Map<String, Object>>>() {});
            
            response.append("📊 Inserting Facts:\n");
            response.append("-".repeat(17) + "\n");
            for (Map<String, Object> fact : facts) {
                currentSession.insert(fact);
                response.append("  • ").append(fact).append("\n");
            }
            
            // Fire rules
            response.append("\n🔥 Firing Rules:\n");
            response.append("-".repeat(15) + "\n");
            int rulesCount;
            if (maxActivations > 0) {
                rulesCount = currentSession.fireAllRules(maxActivations);
            } else {
                rulesCount = currentSession.fireAllRules();
            }
            
            response.append("  • Rules Fired: ").append(rulesCount).append("\n");
            response.append("  • Facts in Working Memory: ").append(currentSession.getFactCount()).append("\n");
            
            response.append("\n✅ Rule execution completed successfully!\n");
            response.append("Knowledge base '").append(knowledgeBaseName).append("' session remains active for further operations.\n");
            
            return response.toString();
            
        } catch (Exception e) {
            return "❌ Rule execution failed: " + e.getMessage();
        }
    }
    
    @Tool("Clear all facts from the session")
    public String clearFacts() {
        try {
            if (currentSession == null) {
                return "❌ No active session found.";
            }
            
            // Get current fact count
            long factCount = currentSession.getFactCount();
            
            // Clear all facts
            currentSession.getFactHandles().forEach(currentSession::delete);
            
            return "✅ Cleared " + factCount + " facts from the session.\n" +
                   "Session is ready for new fact insertions.";
                   
        } catch (Exception e) {
            return "❌ Failed to clear facts: " + e.getMessage();
        }
    }
    
    @Tool("Dispose current knowledge base and session")
    public String disposeKnowledgeBase() {
        try {
            if (currentSession != null) {
                currentSession.dispose();
                currentSession = null;
            }
            
            currentKnowledgeBase = null;
            knowledgeBaseName = null;
            
            return "✅ Knowledge base and session disposed successfully.\n" +
                   "Resources have been freed. You can now create a new knowledge base.";
                   
        } catch (Exception e) {
            return "❌ Failed to dispose knowledge base: " + e.getMessage();
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private long countRules(String drlContent) {
        return drlContent.lines()
            .filter(line -> line.trim().startsWith("rule "))
            .count();
    }
    
    private long countDeclarations(String drlContent) {
        return drlContent.lines()
            .filter(line -> line.trim().startsWith("declare "))
            .count();
    }
    
    private long countGlobals(String drlContent) {
        return drlContent.lines()
            .filter(line -> line.trim().startsWith("global "))
            .count();
    }
}
