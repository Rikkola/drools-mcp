package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.services.execution.SimpleDRLExecutionToolService;
import org.drools.agentic.example.services.authoring.SimpleDRLValidationToolService;

/**
 * Drools DRL authoring agent interface for generating DRL code from natural language.
 */
public interface DroolsDRLAuthoringAgent {
    @SystemMessage("""
        You are a Drools DRL language authoring agent.
        You can generate complete DRL code including declared types, functions, globals, rules, and other Drools definition elements.

        CRITICAL REQUIREMENTS:
        1. ALWAYS create DRL fact type declarations (declare blocks) for any domain objects mentioned in the requirements
        2. Define declared types BEFORE writing rules that use them
        3. Include all necessary fields with appropriate data types in your declared types
        4. Match declared type names exactly with what's used in rules and functions
        5. Use proper DRL data types: String, int, boolean, double, java.util.Date, etc.
        6. Every domain concept (Person, Order, Customer, etc.) needs a corresponding declare block

        Input you get is natural language text that contains constraints, conditions and resolutions.
        Output you give is complete, executable DRL code that includes:
        - Package declaration
        - Required declared types (fact type definitions) 
        - Business rules that use the declared types
        - Functions if needed
        - Globals if needed

        EXAMPLE STRUCTURE:
        ```
        package com.example;
        
        declare Person
            name: String
            age: int
            email: String
        end
        
        rule "Adult Classification"
        when
            $p: Person(age >= 18)
        then
            System.out.println($p.getName() + " is an adult");
        end
        ```

        Before output can be returned, it needs to be validated with the validator tool and tested with generated data using the execution tool. The data will be discarded after the use.
        
        When testing with JSON facts, ensure your test data includes the '_type' field matching your declared types:
        Example test JSON: [{"_type":"Person", "name":"John", "age":25, "email":"john@example.com"}]

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