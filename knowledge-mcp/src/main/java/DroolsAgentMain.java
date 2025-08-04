package dev.langchain4j.agentic.example;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import org.drools.storage.DefinitionStorage;

public class DroolsAgentMain {

    public static void main(String[] args) {
        ChatModel chatModel = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-3-haiku-20240307")
                .logRequests(true)
                .logResponses(true)
                .build();

        // Create the supervisor agent using DroolsAgent factory method
        SupervisorAgent droolsSupervisorAgent = DroolsAgent.createDroolsSupervisorAgent(chatModel);

        // Example 1: Use supervisor agent for complete workflow
        System.out.println("=== Supervisor Agent Demo ===");
        String result1 = droolsSupervisorAgent.invoke("""
            Create a Person type with name, age, and adult fields, then validate and execute it with JSON facts for John age 25 and Jane age 16.
            """);
        System.out.println("Supervisor Result:");
        System.out.println(result1);

        // Create individual agents for demonstration (alternative approach)
        demonstrateIndividualAgents(chatModel);
        
        // Demonstrate direct service usage
        demonstrateDirectServices();
    }

    private static void demonstrateIndividualAgents(ChatModel chatModel) {
        // For demonstration purposes, we can also create individual agents
        DefinitionStorage sharedStorage = new DefinitionStorage();
        DefinitionStorageService definitionService = new DefinitionStorageService(sharedStorage);
        DRLExecutionToolService executionService = new DRLExecutionToolService(sharedStorage);
        DRLValidationToolService validationService = new DRLValidationToolService(sharedStorage);

        // Build individual specialized agents
        DroolsAgent.DroolsDefinitionAgent definitionAgent = dev.langchain4j.agentic.AgentServices.agentBuilder(DroolsAgent.DroolsDefinitionAgent.class)
                .chatModel(chatModel)
                .tools(definitionService)
                .build();

        DroolsAgent.DRLExecutionAgent executionAgent = dev.langchain4j.agentic.AgentServices.agentBuilder(DroolsAgent.DRLExecutionAgent.class)
                .chatModel(chatModel)
                .tools(executionService)
                .build();

        DroolsAgent.DRLValidationAgent validationAgent = dev.langchain4j.agentic.AgentServices.agentBuilder(DroolsAgent.DRLValidationAgent.class)
                .chatModel(chatModel)
                .tools(validationService)
                .build();

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
    }

    private static void demonstrateDirectServices() {
        // Direct service demonstration
        System.out.println("\n=== Direct Service Demo ===");
        
        DefinitionStorage sharedStorage = new DefinitionStorage();
        DefinitionStorageService definitionService = new DefinitionStorageService(sharedStorage);
        DRLExecutionToolService executionService = new DRLExecutionToolService(sharedStorage);
        DRLValidationToolService validationService = new DRLValidationToolService(sharedStorage);
        
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