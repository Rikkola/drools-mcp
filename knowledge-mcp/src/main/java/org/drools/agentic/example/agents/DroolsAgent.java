package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.services.DefinitionStorageService;
import org.drools.agentic.example.services.SimpleDRLValidationToolService;
import org.drools.agentic.example.services.SimpleDRLExecutionToolService;
import org.drools.storage.DefinitionStorage;

public class DroolsAgent {

    public interface DroolsDRLAuthoringAgent {
        @SystemMessage("""
            You are a Drools DRL language authoring agent.
            You can generate DRL code like declared types, functions, globals, rules, and other Drools definition elements.
    
            Input you get is natural language text that contains constraints, conditions and resolutions.
            Output you give is the given text presented in DRL format.

            Before output can be returned. It needs to be validated with the validator tool and tested with generated data using the execution tool. The data will be discarded after the use.

            """)
        @UserMessage("{{request}}")
        @Agent("A Drools DRL authoring agent")
        String handleRequest(@V("request") String request);
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
        SimpleDRLValidationToolService validationService = new SimpleDRLValidationToolService();
        SimpleDRLExecutionToolService executionService = new SimpleDRLExecutionToolService();

        // Build individual specialized agents
        DroolsDRLAuthoringAgent authoringAgent = AgenticServices.agentBuilder(DroolsDRLAuthoringAgent.class)
                .chatModel(chatModel)
                .tools(validationService, executionService)
                .build();

        // Build and return supervisor agent that coordinates the specialized agents
        return AgenticServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(authoringAgent)
                .build();
    }
}
