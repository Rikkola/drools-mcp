package org.drools.agentic.example.main;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsAgent;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.services.DefinitionStorageService;
import org.drools.agentic.example.services.DRLExecutionToolService;
import org.drools.agentic.example.services.DRLValidationToolService;
import org.drools.storage.DefinitionStorage;

public class DroolsAgentMain {

    public static void main(String[] args) {
        // Choose model based on environment or preference
        ChatModel chatModel = selectChatModel(args);
        
        System.out.println("Using chat model: " + chatModel.getClass().getSimpleName());

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

    /**
     * Selects the appropriate ChatModel based on command line arguments or environment.
     * 
     * @param args Command line arguments
     * @return Selected ChatModel
     */
    private static ChatModel selectChatModel(String[] args) {
        // Check command line arguments first
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "--ollama":
                case "-o":
                    System.out.println("Using default Ollama model (llama3.2:3b)");
                    return ChatModels.DEFAULT_OLLAMA_MODEL;
                    
                case "--ollama-8b":
                    System.out.println("Using Ollama Llama 8B model");
                    return ChatModels.OLLAMA_LLAMA_8B_MODEL;
                    
                case "--codellama":
                    System.out.println("Using Ollama CodeLlama model");
                    return ChatModels.OLLAMA_CODELLAMA_MODEL;
                    
                case "--anthropic":
                case "-a":
                    System.out.println("Using Anthropic Claude model");
                    return ChatModels.DEFAULT_ANTHROPIC_MODEL;
                    
                case "--auto":
                    System.out.println("Auto-selecting model from environment");
                    return ChatModels.createFromEnvironment();
            }
        }
        
        // Check for model-specific arguments
        for (int i = 0; i < args.length - 1; i++) {
            if ("--ollama-model".equals(args[i])) {
                String modelName = args[i + 1];
                System.out.println("Using custom Ollama model: " + modelName);
                return ChatModels.createOllamaModel(modelName);
            }
            if ("--ollama-url".equals(args[i])) {
                String baseUrl = args[i + 1];
                String modelName = (i + 2 < args.length) ? args[i + 2] : "llama3.2:3b";
                System.out.println("Using Ollama at " + baseUrl + " with model: " + modelName);
                return ChatModels.createOllamaModel(baseUrl, modelName);
            }
        }
        
        // Default: try environment-based selection, fallback to Anthropic
        ChatModel envModel = ChatModels.createFromEnvironment();
        if (envModel.getClass().getSimpleName().contains("Ollama")) {
            System.out.println("Auto-selected Ollama model from environment");
        } else {
            System.out.println("Using default Anthropic model");
        }
        return envModel;
    }
}