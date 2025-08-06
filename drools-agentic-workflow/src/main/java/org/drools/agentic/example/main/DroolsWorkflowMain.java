package org.drools.agentic.example.main;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsService;
import org.drools.agentic.example.agents.DroolsSupervisor;
import org.drools.agentic.example.agents.DRLExecutionAgent;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.services.DRLExecutionToolService;
import org.drools.agentic.example.services.DRLValidationToolService;
import org.drools.storage.DefinitionStorage;

public class DroolsWorkflowMain {

    public static void main(String[] args) {
        // Choose models based on environment or preference
        ChatModel planningModel = selectPlanningModel(args);
        ChatModel codeGenModel = selectCodeGenModel(args);
        
        System.out.println("Using planning model: " + planningModel.getClass().getSimpleName());
        System.out.println("Using code generation model: " + codeGenModel.getClass().getSimpleName());

        // Create the supervisor agent using DroolsService factory method with separate models
        DroolsSupervisor droolsSupervisorAgent = DroolsService.createDroolsSupervisorAgent(planningModel, codeGenModel);

        // Example 1: Use supervisor agent for complete workflow
        System.out.println("=== Supervisor Agent Demo ===");
        String result1 = droolsSupervisorAgent.invoke("""
            Create a Person type with name, age, and adult fields. Then create rules that check if a person is an adult or not.
            """);
        System.out.println("Supervisor Result:");
        System.out.println(result1);

    }

    /**
     * Selects the appropriate planning ChatModel based on command line arguments or environment.
     * 
     * @param args Command line arguments
     * @return Selected planning ChatModel
     */
    private static ChatModel selectPlanningModel(String[] args) {
        // Check command line arguments first
        for (String arg : args) {
            if (arg.startsWith("--planning=")) {
                String modelName = arg.substring("--planning=".length());
                System.out.println("Using custom planning model: " + modelName);
                return ChatModels.createOllamaModel(modelName);
            }
            switch (arg.toLowerCase()) {
                case "--granite":
                case "-g":
                    System.out.println("Using Granite planning model (granite-code:20b)");
                    return ChatModels.OLLAMA_GRANITE_MODEL;
                    
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
        
        // Default: use Granite code model (best for planning)
        System.out.println("Using default Granite planning model (granite-code:20b)");
        return ChatModels.OLLAMA_GRANITE_MODEL;
    }

    /**
     * Selects the appropriate code generation ChatModel based on command line arguments or environment.
     * 
     * @param args Command line arguments
     * @return Selected code generation ChatModel
     */
    private static ChatModel selectCodeGenModel(String[] args) {
        // Check command line arguments first
        for (String arg : args) {
            if (arg.startsWith("--codegen=")) {
                String modelName = arg.substring("--codegen=".length());
                System.out.println("Using custom code generation model: " + modelName);
                return ChatModels.createOllamaModel(modelName);
            }
            switch (arg.toLowerCase()) {
                case "--granite3-moe":
                    System.out.println("Using Granite3 MoE code generation model (granite3-moe:3b)");
                    return ChatModels.OLLAMA_GRANITE3_MOE_MODEL;
                    
                case "--granite":
                case "-g":
                    System.out.println("Using Granite3 MoE code generation model (granite3-moe:3b)");
                    return ChatModels.OLLAMA_GRANITE3_MOE_MODEL;
                    
                case "--qwen":
                case "--qwen-coder":
                    System.out.println("Using Qwen2.5 Coder code generation model (qwen2.5-coder:14b)");
                    return ChatModels.createOllamaModel("qwen2.5-coder:14b");
                    
                case "--ollama":
                case "-o":
                    System.out.println("Using default Ollama code generation model (llama3.2:3b)");
                    return ChatModels.DEFAULT_OLLAMA_MODEL;
                    
                case "--ollama-8b":
                    System.out.println("Using Ollama Llama 8B code generation model");
                    return ChatModels.OLLAMA_LLAMA_8B_MODEL;
                    
                case "--codellama":
                    System.out.println("Using Ollama CodeLlama code generation model");
                    return ChatModels.OLLAMA_CODELLAMA_MODEL;
                    
                case "--anthropic":
                case "-a":
                    System.out.println("Using Anthropic Claude code generation model");
                    return ChatModels.DEFAULT_ANTHROPIC_MODEL;
                    
                case "--auto":
                    System.out.println("Auto-selecting code generation model from environment");
                    return ChatModels.createFromEnvironment();
            }
        }
        
        // Check for model-specific arguments
        for (int i = 0; i < args.length - 1; i++) {
            if ("--ollama-model".equals(args[i])) {
                String modelName = args[i + 1];
                System.out.println("Using custom Ollama code generation model: " + modelName);
                return ChatModels.createOllamaModel(modelName);
            }
            if ("--ollama-url".equals(args[i])) {
                String baseUrl = args[i + 1];
                String modelName = (i + 2 < args.length) ? args[i + 2] : "granite3-moe:3b";
                System.out.println("Using Ollama at " + baseUrl + " for code generation with model: " + modelName);
                return ChatModels.createOllamaModel(baseUrl, modelName);
            }
        }
        
        // Default: use Qwen2.5 Coder model (fast and supports tools)
        System.out.println("Using default Qwen2.5 Coder code generation model (qwen2.5-coder:14b)");
        return ChatModels.createOllamaModel("qwen2.5-coder:14b");
    }
}
