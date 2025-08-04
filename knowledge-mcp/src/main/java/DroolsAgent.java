package dev.langchain4j.agentic.example;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgentServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
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

    // Define a comprehensive Drools agent that combines both definition management and execution
    public interface ComprehensiveDroolsAgent {
        @SystemMessage("""
            You are a comprehensive Drools assistant that helps users with both rule definition management and execution.
            You can store, retrieve, and organize DRL definitions, as well as execute rules with data and analyze results.
            Use the appropriate tools based on what the user needs - definition management or rule execution.
            Always provide helpful and detailed responses about both the definitions and execution results.
            """)
        @UserMessage("{{request}}")
        @Agent("A comprehensive Drools management and execution agent")
        String handleRequest(@V("request") String request);
    }


    public static void main(String[] args) {
        ChatModel chatModel = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-3-haiku-20240307")
                .logRequests(true)
                .logResponses(true)
                .build();

        // Create shared storage and service instances
        DefinitionStorage sharedStorage = new DefinitionStorage();
        DefinitionStorageService definitionService = new DefinitionStorageService(sharedStorage);
        DRLExecutionToolService executionService = new DRLExecutionToolService(sharedStorage);

        // Build individual specialized agents
        DroolsDefinitionAgent definitionAgent = AgentServices.agentBuilder(DroolsDefinitionAgent.class)
                .chatModel(chatModel)
                .tools(definitionService)
                .build();

        DRLExecutionAgent executionAgent = AgentServices.agentBuilder(DRLExecutionAgent.class)
                .chatModel(chatModel)
                .tools(executionService)
                .build();

        // Build comprehensive agent with both definition and execution tools
        ComprehensiveDroolsAgent comprehensiveAgent = AgentServices.agentBuilder(ComprehensiveDroolsAgent.class)
                .chatModel(chatModel)
                .tools(definitionService, executionService)
                .build();

        // Example 1: Use comprehensive agent for complete workflow
        System.out.println("=== Comprehensive Agent Demo ===");
        String result1 = comprehensiveAgent.handleRequest("""
            Create a Person type with name, age, and adult fields, then add a rule that sets adult=true for people over 18.
            After that, execute it with JSON facts for John age 25 and Jane age 16.
            """);
        System.out.println("Comprehensive Result:");
        System.out.println(result1);

        // Example 2: Use specialized definition agent
        System.out.println("\n=== Definition Agent Demo ===");
        String result2 = definitionAgent.handleRequest("Add an Order type with id, amount, and discount fields");
        System.out.println("Definition Result:");
        System.out.println(result2);

        // Example 3: Use specialized execution agent
        System.out.println("\n=== Execution Agent Demo ===");
        String result3 = executionAgent.executeRequest("""
            Execute this DRL code with JSON facts:
            DRL: rule "test" when then System.out.println("Hello Drools!"); end
            JSON: []
            """);
        System.out.println("Execution Result:");
        System.out.println(result3);

        // Direct service demonstration
        System.out.println("\n=== Direct Service Demo ===");
        
        // Add some definitions directly to shared storage
        sharedStorage.addDefinition("Customer", "declare", 
            "declare Customer\n    name: String\n    vip: boolean\nend");
        sharedStorage.addDefinition("VIPRule", "rule", 
            "rule \"Mark VIP customers\"\nwhen\n    $c: Customer(name == \"Premium User\")\nthen\n    $c.setVip(true);\nend");
        
        // Execute against stored definitions
        String directResult = executionService.executeStoredDefinitionsWithJsonFacts(
            "[{\"name\": \"Premium User\", \"vip\": false}, {\"name\": \"Regular User\", \"vip\": false}]", 
            10);
        System.out.println("Direct Execution Result:");
        System.out.println(directResult);
    }
}
