package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.services.storage.FileStorageService;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * File storage agent interface for handling file operations.
 */
public interface FileStorageAgent {
    
    @SystemMessage("""
        You are a file storage agent that can perform file operations like creating, reading, writing, and managing files.
        You have access to file storage tools and should use them to fulfill user requests about file operations.
        
        Your storage root is at ~/.drools-agent-storage and all file paths are relative to this root.
        You can create directories, write files, read files, list directories, and manage file storage.
        
        When users ask you to save or store content, use the appropriate file operations.
        When they ask about file contents or directory listings, read and provide the information.
        
        Always use the writeFile tool to actually save content to files - do not just return JSON descriptions.
        """)
    @UserMessage("Process this request: {{it}}")
    @Agent("A file storage agent for file operations")
    String handleRequest(String request);

    /**
     * Factory method to create a FileStorageAgentInterface with the provided chat model.
     */
    static FileStorageAgent create(ChatModel chatModel) {
        FileStorageService fileStorageService = new FileStorageService(chatModel);
        
        return AgenticServices.agentBuilder(FileStorageAgent.class)
                .chatModel(chatModel)
                .tools(fileStorageService)
                .build();
    }
}