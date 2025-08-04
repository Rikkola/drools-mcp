package dev.langchain4j.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgentServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.agentic.example.services.DefinitionStorageService;
import dev.langchain4j.agentic.example.services.DRLExecutionToolService;
import dev.langchain4j.agentic.example.services.DRLValidationToolService;
import org.drools.storage.DefinitionStorage;

public class DroolsAgent {

    // Define the Drools definition management agent
    public interface DroolsDefinitionAgent {
        @SystemMessage("""
            You are a Drools definition management assistant that helps users manage their DRL definitions.
            You can store, retrieve, organize, and generate DRL code from declared types, functions, globals, and other Drools definitions.
            Always provide helpful responses and use the available tools to perform the requested operations.
            """)
        @UserMessage("{{request}}")
        @Agent("A Drools definition management agent")
        String handleRequest(@V("request") String request);
    }

    // Define the DRL execution agent
    public interface DRLExecutionAgent {
        @SystemMessage("""
            You are a Drools rule execution assistant that helps users execute DRL rules and analyze results.
            You can execute DRL code with JSON facts, run stored definitions against data, and analyze execution results.
            Always provide clear feedback about rule execution including facts processed and working memory contents.
            """)
        @UserMessage("{{request}}")
        @Agent("A DRL execution agent")
        String executeRequest(@V("request") String request);
    }

    // Define the DRL validation agent
    public interface DRLValidationAgent {
        @SystemMessage("""
            You are a Drools validation assistant that helps users validate their DRL code and definitions.
            You can check DRL syntax, structure, and provide guidance on fixing validation issues.
            Always provide clear feedback about validation results and helpful suggestions for improvement.
            """)
        @UserMessage("{{request}}")
        @Agent("A DRL validation agent")
        String validateRequest(@V("request") String request);
    }



    /**
     * Creates and returns a SupervisorAgent that coordinates all Drools agents.
     * 
     * @param chatModel The chat model to use for all agents
     * @return A configured SupervisorAgent ready for use
     */
    public static SupervisorAgent createDroolsSupervisorAgent(ChatModel chatModel) {
        // Create shared storage and service instances
        DefinitionStorage sharedStorage = new DefinitionStorage();
        DefinitionStorageService definitionService = new DefinitionStorageService(sharedStorage);
        DRLExecutionToolService executionService = new DRLExecutionToolService(sharedStorage);
        DRLValidationToolService validationService = new DRLValidationToolService(sharedStorage);

        // Build individual specialized agents
        DroolsDefinitionAgent definitionAgent = AgentServices.agentBuilder(DroolsDefinitionAgent.class)
                .chatModel(chatModel)
                .tools(definitionService)
                .build();

        DRLExecutionAgent executionAgent = AgentServices.agentBuilder(DRLExecutionAgent.class)
                .chatModel(chatModel)
                .tools(executionService)
                .build();

        DRLValidationAgent validationAgent = AgentServices.agentBuilder(DRLValidationAgent.class)
                .chatModel(chatModel)
                .tools(validationService)
                .build();

        // Build and return supervisor agent that coordinates the specialized agents
        return AgentServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(definitionAgent, executionAgent, validationAgent)
                .build();
    }
}