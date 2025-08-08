package org.drools.agentic.example.services.knowledge;

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
import org.drools.agentic.example.storage.KnowledgeBaseStorage;

/**
 * Drools knowledge base service that builds and manages Drools knowledge bases from DRL files.
 * Can read DRL files from storage and build executable knowledge bases.
 */
public class DroolsKnowledgeBaseService {
    
    private final ChatModel chatModel;
    private final Path storageRoot;
    
    private final KnowledgeBaseStorage storage = KnowledgeBaseStorage.getInstance();
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
                // If specific file not found, try to find any .drl file in storage
                return buildKnowledgeBaseFromAllFiles();
            }
            
            String drlContent = Files.readString(filePath);
            return buildKnowledgeBaseFromContent(drlContent);
            
        } catch (IOException e) {
            return "❌ Error reading DRL file " + filename + ": " + e.getMessage();
        }
    }
    
    @Tool("Build a Drools knowledge base from all DRL files in storage")
    public String buildKnowledgeBaseFromAllFiles() {
        try {
            List<Path> drlFiles = Files.list(storageRoot)
                .filter(path -> path.toString().toLowerCase().endsWith(".drl"))
                .toList();
            
            if (drlFiles.isEmpty()) {
                return "❌ No DRL files found in storage directory: " + storageRoot;
            }
            
            StringBuilder combinedDrl = new StringBuilder();
            StringBuilder fileList = new StringBuilder();
            
            for (Path drlFile : drlFiles) {
                String content = Files.readString(drlFile);
                combinedDrl.append(content).append("\n\n");
                fileList.append("  • ").append(drlFile.getFileName()).append("\n");
            }
            
            String result = buildKnowledgeBaseFromContent(combinedDrl.toString());
            
            // Prepend file information to result
            return "📁 Found " + drlFiles.size() + " DRL file(s):\n" + fileList + "\n" + result;
            
        } catch (IOException e) {
            return "❌ Error reading DRL files from storage: " + e.getMessage();
        }
    }
    
    private String buildKnowledgeBaseFromContent(String drlContent) {
        try {
            String name = "main-kb";
            
            // Previous knowledge base will be disposed by shared storage
            
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
            
            // Create container and session, store in shared storage
            KieContainer kieContainer = ks.newKieContainer(kieBuilder.getKieModule().getReleaseId());
            KieSession kieSession = kieContainer.newKieSession();
            
            // Store in shared storage
            storage.store(name, kieContainer, kieSession, resourceName);
            
            response.append("✅ Knowledge Base Built and Stored Successfully!\n");
            response.append("-".repeat(45) + "\n");
            response.append("📋 Knowledge Base Details:\n");
            response.append("  • Name: ").append(name).append("\n");
            response.append("  • Release ID: ").append(kieBuilder.getKieModule().getReleaseId()).append("\n");
            response.append("  • Resource: ").append(resourceName).append("\n");
            response.append("  • Session Created: Yes\n");
            response.append("  • Session ID: ").append(kieSession.getId()).append("\n");
            
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
        
        if (!storage.hasKnowledgeBase()) {
            response.append("No knowledge base currently loaded.\n");
            response.append("Use 'buildKnowledgeBaseFromFile' or 'buildKnowledgeBaseFromContent' to create one.\n");
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
        
        response.append("\n🎯 Ready for rule execution!\n");
        
        return response.toString();
    }
    
    @Tool("Dispose current knowledge base and session")
    public String disposeKnowledgeBase() {
        try {
            storage.dispose();
            
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
