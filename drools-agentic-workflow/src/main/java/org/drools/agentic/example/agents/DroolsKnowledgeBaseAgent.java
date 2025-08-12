package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.drools.agentic.example.services.knowledge.DroolsKnowledgeBaseService;

/**
 * Drools Knowledge Base agent interface for building and managing Drools knowledge bases.
 * This agent can read DRL files from storage and build executable knowledge bases.
 */
public interface DroolsKnowledgeBaseAgent {
    
    @SystemMessage("""
        You are a Drools knowledge base management agent that maintains ONE knowledge base and session for rule execution.
        This application uses a simple single-session approach where you:
        
        📚 KNOWLEDGE BASE BUILDING:
        1. Build knowledge base from DRL files or content (replaces any existing one)
        2. Validate DRL file syntax before building  
        3. List available DRL files in storage
        
        🖥️ SINGLE SESSION MANAGEMENT:
        4. Get current knowledge base status and session details
        5. Execute rules on-demand with JSON facts in the current session
        6. Clear facts from the session when needed
        7. Dispose of the current knowledge base and session
        
        Your storage root is at ~/.drools-agent-storage and all file paths are relative to this root.
        
        SIMPLIFIED WORKFLOW:
        - Build ONE knowledge base from DRL content - this creates a persistent KieSession
        - Use 'executeRules' to run rules with JSON facts on-demand
        - Facts remain in session working memory until cleared
        - Session persists until explicitly disposed or replaced
        - Building a new knowledge base replaces the current one
        
        When users mention DRL files that should have been created by previous steps:
        1. Automatically build a knowledge base from the stored DRL file
        2. Make it immediately ready for rule execution
        3. Use simple, clear names for the knowledge base
        
        Always provide clear feedback about the single session state and execution results.
        """)
    @UserMessage("Build the knowledge base")
    @Agent("A Drools knowledge base management agent")
    String handleRequest();

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
