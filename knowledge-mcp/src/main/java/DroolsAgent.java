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

        // Build supervisor agent that coordinates the specialized agents
        SupervisorAgent droolsSupervisorAgent = AgentServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(definitionAgent, executionAgent, validationAgent)
                .build();

        // Example 1: Use supervisor agent for complete workflow
        System.out.println("=== Supervisor Agent Demo ===");
        String result1 = droolsSupervisorAgent.invoke("""
            Create a Person type with name, age, and adult fields, then validate and execute it with JSON facts for John age 25 and Jane age 16.
            """);
        System.out.println("Supervisor Result:");
        System.out.println(result1);

        // Example 2: Use specialized validation agent
        System.out.println("\n=== Validation Agent Demo ===");
        String result2 = validationAgent.validateRequest("""
            Please validate this DRL code and provide guidance:
            rule "adult check"
            when
                $p: Person(age > 18)
            then
                $p.setAdult(true);
            end
            """);
        System.out.println("Validation Result:");
        System.out.println(result2);

        // Example 3: Use specialized definition agent
        System.out.println("\n=== Definition Agent Demo ===");
        String result3 = definitionAgent.handleRequest("Add an Order type with id, amount, and discount fields");
        System.out.println("Definition Result:");
        System.out.println(result3);

        // Example 4: Use specialized execution agent
        System.out.println("\n=== Execution Agent Demo ===");
        String result4 = executionAgent.executeRequest("""
            Execute JSON facts against stored definitions:
            JSON: [{"name": "Test User", "age": 30}]
            """);
        System.out.println("Execution Result:");
        System.out.println(result4);

        // Direct service demonstration
        System.out.println("\n=== Direct Service Demo ===");
        
        // Add some definitions directly to shared storage
        sharedStorage.addDefinition("Customer", "declare", 
            "declare Customer\n    name: String\n    vip: boolean\nend");
        sharedStorage.addDefinition("VIPRule", "rule", 
            "rule \"Mark VIP customers\"\nwhen\n    $c: Customer(name == \"Premium User\")\nthen\n    $c.setVip(true);\nend");
        
        // Validate stored definitions
        String validationResult = validationService.validateStoredDefinitions();
        System.out.println("Direct Validation Result:");
        System.out.println(validationResult);
        
        // Execute against stored definitions
        String executionResult = executionService.executeWithJsonFacts(
            "[{\"name\": \"Premium User\", \"vip\": false}, {\"name\": \"Regular User\", \"vip\": false}]", 
            10);
        System.out.println("Direct Execution Result:");
        System.out.println(executionResult);
        
        // Validate a problematic DRL snippet
        System.out.println("\n=== Validation Demo with Issues ===");
        String problemValidation = validationService.validateWithGuidance(
            "rule incomplete\nwhen\n    Person(age >\nthen\n    // missing end");
        System.out.println("Problem Validation Result:");
        System.out.println(problemValidation);
    }
}
