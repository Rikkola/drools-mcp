package org.drools.agentic.example.main;

import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.config.ChatModels;

/**
 * Reusable model selector utility for main package classes.
 * Provides standardized ChatModel configurations optimized for DRL code generation tasks,
 * with support for both single-model and dual-model workflows.
 */
public class ModelSelector {

    /**
     * Available model types for DRL code generation.
     */
    public enum ModelType {
        /** Anthropic Claude - Excellent code reasoning and tool usage */
        ANTHROPIC_CLAUDE("claude-3-haiku-20240307", "Anthropic Claude Haiku"),
        
        /** IBM Granite 3.3 8B - Best for planning with enhanced reasoning */
        GRANITE_PLANNING("granite3.3:8b", "IBM Granite 3.3 8B"),
        
        /** IBM Granite Code 20B - Optimized for code generation */
        GRANITE_CODE("granite-code:20b", "IBM Granite Code 20B"),
        
        /** Qwen3 14B - Optimized for tool calling and function usage */
        QWEN_TOOLS("qwen3:14b", "Qwen3 14B Tools"),
        
        /** Qwen2.5 Coder 14B - Advanced code generation and planning with tool support */
        QWEN_CODER("qwen2.5-coder:14b-instruct-q4_K_M", "Qwen2.5 Coder 14B"),
        
        /** IBM Granite 3.3 8B - Balanced planning model for resource efficiency */
        GRANITE_33("granite3.3:8b", "IBM Granite 3.3 8B Balanced"),
        
        /** IBM Granite3 MoE 3B - Lightweight high-performance model */
        GRANITE_MOE("granite3-moe:3b", "IBM Granite3 MoE 3B");

        private final String modelName;
        private final String displayName;

        ModelType(String modelName, String displayName) {
            this.modelName = modelName;
            this.displayName = displayName;
        }

        public String getModelName() { return modelName; }
        public String getDisplayName() { return displayName; }
    }

    /**
     * Creates a ChatModel based on the specified type.
     * 
     * @param modelType The type of model to create
     * @return Configured ChatModel
     * @throws IllegalStateException if required environment variables are missing
     */
    public static ChatModel createChatModel(ModelType modelType) {
        return switch (modelType) {
            case ANTHROPIC_CLAUDE -> ChatModels.DEFAULT_ANTHROPIC_MODEL;
            case GRANITE_PLANNING -> ChatModels.OLLAMA_GRANITE_PLANNING_MODEL;
            case GRANITE_CODE -> ChatModels.OLLAMA_GRANITE_CODE_MODEL;
            case QWEN_TOOLS -> ChatModels.OLLAMA_QWEN_TOOL_MODEL;
            case QWEN_CODER -> ChatModels.QWEN_CODER_14B_MODEL;
            case GRANITE_33 -> ChatModels.GRANITE_33_8B_MODEL;
            case GRANITE_MOE -> ChatModels.OLLAMA_GRANITE3_MOE_MODEL;
        };
    }

    /**
     * Lists available models with their characteristics for DRL code generation.
     */
    public static void printAvailableModels() {
        System.out.println("📋 Available Models for DRL Code Generation:");
        System.out.println("=" .repeat(50));
        
        for (ModelType type : ModelType.values()) {
            String marker = (type == ModelType.QWEN_CODER) ? " [DEFAULT]" : "";
            System.out.printf("🤖 %-20s - %s%s%n", type.name(), type.getDisplayName(), marker);
        }
        
        System.out.println("\n💡 Usage:");
        System.out.println("   Command line: --granite, --anthropic");
        System.out.println("   Environment: export MODEL_TYPE=GRANITE_CODE");
        System.out.println("   Custom: --codegen=your-model-name");
        System.out.println("   Auto: --auto (uses environment detection)");
        
        System.out.println("\n🎯 Model Recommendations:");
        System.out.println("   • Planning/Coordination: Granite 3.3 8B Instruct (--granite) [default for code gen]");
        System.out.println("   • Fast Code Generation: Qwen3 14B (--qwen-coder)");
        System.out.println("   • Cloud/Quality: Anthropic Claude (--anthropic)");
        System.out.println("   • Lightweight: Granite3 MoE 3B (--granite3-moe)");
    }

    /**
     * Creates planning ChatModel for workflows with separate planning/codegen models.
     * 
     * @param args Command line arguments
     * @return Configured ChatModel for planning tasks
     */
    public static ChatModel createPlanningModelFromArgs(String[] args) {
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
                    System.out.println("Using Granite planning model (granite3.3:8b)");
                    return createChatModel(ModelType.GRANITE_PLANNING);
                    
                case "--anthropic":
                case "-a":
                    System.out.println("Using Anthropic Claude planning model");
                    return createChatModel(ModelType.ANTHROPIC_CLAUDE);
                    
                case "--auto":
                    System.out.println("Auto-selecting planning model from environment");
                    return ChatModels.createFromEnvironment();
            }
        }
        
        // Default to qwen2.5-coder:14b for planning (optimized for tool calling, coordination, and code understanding)
        System.out.println("Using default Qwen2.5 Coder planning model (qwen2.5-coder:14b-instruct-q4_K_M)");
        return createChatModel(ModelType.QWEN_CODER);
    }

    /**
     * Creates code generation ChatModel for workflows with separate planning/codegen models.
     * 
     * @param args Command line arguments
     * @return Configured ChatModel for code generation tasks
     */
    public static ChatModel createCodeGenModelFromArgs(String[] args) {
        // Check command line arguments first
        for (String arg : args) {
            if (arg.startsWith("--codegen=")) {
                String modelName = arg.substring("--codegen=".length());
                System.out.println("Using custom code generation model: " + modelName);
                return ChatModels.createOllamaModel(modelName);
            }
            switch (arg.toLowerCase()) {
                case "--granite-code":
                case "--granite":
                case "-g":
                    System.out.println("Using Granite code generation model (granite-code:20b)");
                    return createChatModel(ModelType.GRANITE_CODE);
                    
                case "--anthropic":
                case "-a":
                    System.out.println("Using Anthropic Claude code generation model");
                    return createChatModel(ModelType.ANTHROPIC_CLAUDE);
                    
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
                String modelName = (i + 2 < args.length) ? args[i + 2] : "qwen3:14b";
                System.out.println("Using Ollama at " + baseUrl + " for code generation with model: " + modelName);
                return ChatModels.createOllamaModel(baseUrl, modelName);
            }
        }
        
        return createChatModel(ModelType.GRANITE_MOE);
    }

}
