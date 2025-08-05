package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.services.SimpleDRLExecutionToolService;
import org.drools.agentic.example.services.SimpleDRLValidationToolService;

/**
 * Drools DRL authoring agent interface for generating DRL code from natural language.
 */
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

    /**
     * Creates a DroolsDRLAuthoringAgent with simple validation and execution tools.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @return A configured DroolsDRLAuthoringAgent with validation and execution tools
     */
    static DroolsDRLAuthoringAgent create(ChatModel chatModel) {
        SimpleDRLValidationToolService validationService = new SimpleDRLValidationToolService();
        SimpleDRLExecutionToolService executionService = new SimpleDRLExecutionToolService();

        return AgenticServices.agentBuilder(DroolsDRLAuthoringAgent.class)
                .chatModel(chatModel)
                .tools(validationService, executionService)
                .build();
    }
}