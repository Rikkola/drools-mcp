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
import java.util.stream.Collectors;

/**
 * Drools knowledge base service that builds and manages Drools knowledge bases from DRL files.
 * Can read DRL files from storage and build executable knowledge bases.
 */
public class DroolsKnowledgeBaseService {
    
    private final ChatModel chatModel;
    private final Path storageRoot;
    
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
    
    @Tool("Build a Drools knowledge base from DRL content")
    public String buildKnowledgeBaseFromContent(
            @P("The DRL content to build") String drlContent,
            @P("Optional name for the knowledge base") String name) {
        try {
            StringBuilder response = new StringBuilder();
            response.append("🏗️ Building Drools Knowledge Base").append(name != null ? " (" + name + ")" : "").append(":\n");
            response.append("=".repeat(50) + "\n\n");
            
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            
            // Add the DRL content to the file system
            String resourceName = "src/main/resources/" + (name != null ? name : "rules.drl");
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
            
            // Create container and session to verify the build
            KieContainer kieContainer = ks.newKieContainer(kieBuilder.getKieModule().getReleaseId());
            KieSession kieSession = kieContainer.newKieSession();
            
            response.append("✅ Knowledge Base Built Successfully!\n");
            response.append("-".repeat(35) + "\n");
            response.append("📋 Knowledge Base Details:\n");
            response.append("  • Release ID: ").append(kieBuilder.getKieModule().getReleaseId()).append("\n");
            response.append("  • Resource: ").append(resourceName).append("\n");
            response.append("  • Session Created: Yes\n");
            
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
            
            long ruleCount = drlContent.lines()
                .filter(line -> line.trim().startsWith("rule "))
                .count();
            long declareCount = drlContent.lines()
                .filter(line -> line.trim().startsWith("declare "))
                .count();
            long globalCount = drlContent.lines()
                .filter(line -> line.trim().startsWith("global "))
                .count();
                
            response.append("  • Rules: ").append(ruleCount).append("\n");
            response.append("  • Declared Types: ").append(declareCount).append("\n");
            response.append("  • Globals: ").append(globalCount).append("\n");
            
            response.append("\n🎯 Knowledge base is ready for use!\n");
            response.append("You can now execute rules by inserting facts into the session.\n");
            
            // Clean up
            kieSession.dispose();
            
            return response.toString();
            
        } catch (Exception e) {
            return String.format("❌ Knowledge base build failed: %s\n\nPlease check your DRL syntax.", e.getMessage());
        }
    }
    
    @Tool("List all DRL files in storage")
    public String listDRLFiles() {
        try {
            if (!Files.exists(storageRoot)) {
                return "📁 Storage directory does not exist yet.";
            }
            
            List<String> drlFiles = Files.list(storageRoot)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".drl"))
                .map(path -> storageRoot.relativize(path).toString())
                .collect(Collectors.toList());
                
            if (drlFiles.isEmpty()) {
                return "📁 No DRL files found in storage directory: " + storageRoot;
            }
            
            StringBuilder response = new StringBuilder();
            response.append("📁 DRL Files in Storage:\n");
            response.append("=".repeat(25) + "\n");
            for (String file : drlFiles) {
                Path fullPath = storageRoot.resolve(file);
                try {
                    long size = Files.size(fullPath);
                    response.append("  • ").append(file).append(" (").append(size).append(" bytes)\n");
                } catch (IOException e) {
                    response.append("  • ").append(file).append(" (size unknown)\n");
                }
            }
            response.append("\nStorage location: ").append(storageRoot);
            
            return response.toString();
            
        } catch (IOException e) {
            return "❌ Error listing DRL files: " + e.getMessage();
        }
    }
    
    @Tool("Validate DRL file syntax by attempting to build knowledge base")
    public String validateDRLFile(@P("The DRL filename (relative to storage root)") String filename) {
        try {
            Path filePath = storageRoot.resolve(filename);
            if (!Files.exists(filePath)) {
                return "❌ DRL file not found: " + filename;
            }
            
            String drlContent = Files.readString(filePath);
            
            StringBuilder response = new StringBuilder();
            response.append("🔍 Validating DRL File: ").append(filename).append("\n");
            response.append("=".repeat(30 + filename.length()) + "\n\n");
            
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            
            // Add the DRL content to the file system
            kfs.write("src/main/resources/" + filename, drlContent);
            
            // Build and check for errors
            KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
            
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                response.append("❌ Validation Failed - Syntax Errors Found:\n");
                response.append("-".repeat(40) + "\n");
                for (Message message : kieBuilder.getResults().getMessages(Message.Level.ERROR)) {
                    response.append("• ").append(message.getText()).append("\n");
                }
                return response.toString();
            }
            
            response.append("✅ Validation Successful!\n");
            response.append("-".repeat(22) + "\n");
            response.append("The DRL file has valid syntax and can be built into a knowledge base.\n");
            
            if (kieBuilder.getResults().hasMessages(Message.Level.WARNING)) {
                response.append("\n⚠️ Warnings (non-critical):\n");
                for (Message message : kieBuilder.getResults().getMessages(Message.Level.WARNING)) {
                    response.append("• ").append(message.getText()).append("\n");
                }
            }
            
            return response.toString();
            
        } catch (IOException e) {
            return "❌ Error validating DRL file " + filename + ": " + e.getMessage();
        } catch (Exception e) {
            return "❌ Validation failed: " + e.getMessage();
        }
    }
}