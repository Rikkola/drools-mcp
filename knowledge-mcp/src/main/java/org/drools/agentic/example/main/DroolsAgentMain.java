package org.drools.agentic.example.main;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsAgent;
import org.drools.agentic.example.agents.DRLExecutionAgent;
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
            Create a Person type with name, age, and adult fields. Then create rules that check if a person is an adult or not.
            """);
        System.out.println("Supervisor Result:");
        System.out.println(result1);

        // Create individual agents for demonstration (alternative approach)
        //XXX Maybe do this // demonstrateIndividualAgents(chatModel);
        
        // Demonstrate direct service usage
        //XXX Maybe do this // demonstrateDirectServices();
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
        
        // Default: use environment-based selection
        System.out.println("Using environment-based model selection");
        return ChatModels.createFromEnvironment();
    }
}
