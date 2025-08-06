package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Drools Knowledge Base agent interface for building and managing Drools knowledge bases.
 * This agent can read DRL files from storage and build executable knowledge bases.
 */
public interface DroolsKnowledgeBaseAgent {
    
    @SystemMessage("""
        You are a Drools knowledge base management agent that specializes in building and validating Drools knowledge bases from DRL files.
        You have access to tools that can:
        
        1. Build knowledge bases from DRL files stored in the file system
        2. Build knowledge bases from DRL content directly
        3. List available DRL files in storage
        4. Validate DRL file syntax
        
        Your storage root is at ~/.drools-agent-storage and all file paths are relative to this root.
        
        When building knowledge bases:
        - Always validate the DRL content first to ensure it can be compiled
        - Provide detailed feedback about the build process including any errors or warnings
        - Report on the knowledge base contents (rules, declared types, globals)
        - Confirm successful creation of KIE sessions
        
        When users mention DRL files that should have been created by previous steps, look for them in storage and build knowledge bases from them.
        Always use the appropriate tools to perform actual knowledge base operations.
        """)
    @UserMessage("Process this request: {{it}}")
    @Agent("A Drools knowledge base management agent")
    String handleRequest(String request);

    /**
     * Factory method to create a DroolsKnowledgeBaseAgent with the provided chat model.
     */
    static DroolsKnowledgeBaseAgent create(ChatModel chatModel) {
        DroolsKnowledgeBaseService knowledgeBaseService = new DroolsKnowledgeBaseService(chatModel);
        
        return AgenticServices.agentBuilder(DroolsKnowledgeBaseAgent.class)
                .chatModel(chatModel)
                .tools(knowledgeBaseService)
                .build();
    }
}