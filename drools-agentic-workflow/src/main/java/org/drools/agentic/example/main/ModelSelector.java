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
        
        /** IBM Granite Code 20B - Best for complex planning and code generation */
        GRANITE_CODE("granite-code:20b", "IBM Granite Code 20B"),
        
        /** Qwen2.5 Coder 14B - Fast code generation with tool support */
        QWEN_CODER("qwen2.5-coder:14b", "Qwen2.5 Coder 14B"),
        
        /** IBM Granite3 MoE 3B - Lightweight with tool support */
        GRANITE3_MOE("granite3-moe:3b", "IBM Granite3 MoE 3B");

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
     * Creates the default ChatModel optimized for DRL code generation tasks.
     * Uses Qwen2.5 Coder as the default balance of quality, speed, and tool support.
     * 
     * @return Configured ChatModel for DRL code generation
     */
    public static ChatModel getDefaultCodeGenAgent() {
        return createChatModel(ModelType.QWEN_CODER);
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
            case GRANITE_CODE -> ChatModels.OLLAMA_GRANITE_MODEL;
            case QWEN_CODER -> ChatModels.createOllamaModel(modelType.getModelName());
            case GRANITE3_MOE -> ChatModels.OLLAMA_GRANITE3_MOE_MODEL;
        };
    }

    /**
     * Creates a ChatModel from command line arguments, environment, or uses default.
     * Supports standard argument patterns for flexible model selection.
     * 
     * @param args Command line arguments
     * @return Configured ChatModel based on arguments, environment, or default
     */
    public static ChatModel createFromArgs(String[] args) {
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
                    return createChatModel(ModelType.GRANITE3_MOE);
                    
                case "--granite":
                case "-g":
                    System.out.println("Using Granite code generation model (granite-code:20b)");
                    return createChatModel(ModelType.GRANITE_CODE);
                    
                case "--qwen":
                case "--qwen-coder":
                    System.out.println("Using Qwen2.5 Coder code generation model (qwen2.5-coder:14b)");
                    return createChatModel(ModelType.QWEN_CODER);
                    
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
                String modelName = (i + 2 < args.length) ? args[i + 2] : "qwen2.5-coder:14b";
                System.out.println("Using Ollama at " + baseUrl + " for code generation with model: " + modelName);
                return ChatModels.createOllamaModel(baseUrl, modelName);
            }
        }
        
        // Default: use Qwen2.5 Coder model (fast and supports tools)
        System.out.println("Using default Qwen2.5 Coder code generation model (qwen2.5-coder:14b)");
        return getDefaultCodeGenAgent();
    }

    /**
     * Creates a ChatModel from environment variables or uses default.
     * Checks MODEL_TYPE environment variable first, then falls back to ChatModels.createFromEnvironment().
     * 
     * @return Configured ChatModel based on environment or default
     */
    public static ChatModel createFromEnvironment() {
        String modelEnv = System.getenv("MODEL_TYPE");
        if (modelEnv != null && !modelEnv.trim().isEmpty()) {
            try {
                ModelType modelType = ModelType.valueOf(modelEnv.toUpperCase().replace("-", "_"));
                System.out.println("ℹ️  Using model from environment: " + modelType.getDisplayName());
                return createChatModel(modelType);
            } catch (IllegalArgumentException e) {
                System.out.println("⚠️  Unknown MODEL_TYPE: " + modelEnv + ", falling back to ChatModels.createFromEnvironment()");
            }
        }
        
        // Fall back to ChatModels environment detection
        return ChatModels.createFromEnvironment();
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
        System.out.println("   Command line: --granite, --qwen-coder, --anthropic, --granite3-moe");
        System.out.println("   Environment: export MODEL_TYPE=QWEN_CODER");
        System.out.println("   Custom: --codegen=your-model-name");
        System.out.println("   Auto: --auto (uses environment detection)");
        
        System.out.println("\n🎯 Model Recommendations:");
        System.out.println("   • Planning/Coordination: Granite Code 20B (--granite)");
        System.out.println("   • Fast Code Generation: Qwen2.5 Coder 14B (--qwen-coder) [default]");
        System.out.println("   • Cloud/Quality: Anthropic Claude (--anthropic)");
        System.out.println("   • Lightweight: Granite3 MoE 3B (--granite3-moe)");
    }

    /**
     * Validates that the selected model is suitable for code generation with tools.
     * Some models like granite-code:20b don't support function calling.
     * 
     * @param modelType The model type to validate
     * @return true if model supports tools, false otherwise
     */
    public static boolean supportsTools(ModelType modelType) {
        return switch (modelType) {
            case ANTHROPIC_CLAUDE, QWEN_CODER, GRANITE3_MOE -> true;
            case GRANITE_CODE -> false; // Good for planning but doesn't support tools
        };
    }

    /**
     * Creates a single ChatModel for workflows where planning model = chat model.
     * Supports the same argument patterns as DroolsWorkflowMain.
     * 
     * @param args Command line arguments
     * @return Configured ChatModel for unified usage
     */
    public static ChatModel createSingleModelFromArgs(String[] args) {
        // Check command line arguments first
        for (String arg : args) {
            if (arg.startsWith("--model=")) {
                String modelName = arg.substring("--model=".length());
                System.out.println("Using custom model: " + modelName);
                return ChatModels.createOllamaModel(modelName);
            }
            switch (arg.toLowerCase()) {
                case "--granite":
                case "-g":
                    System.out.println("Using Granite model (granite-code:20b)");
                    return createChatModel(ModelType.GRANITE_CODE);
                    
                case "--qwen":
                case "--qwen-coder":
                    System.out.println("Using Qwen2.5 Coder model (qwen2.5-coder:14b)");
                    return createChatModel(ModelType.QWEN_CODER);
                    
                case "--granite3-moe":
                    System.out.println("Using Granite3 MoE model (granite3-moe:3b)");
                    return createChatModel(ModelType.GRANITE3_MOE);
                    
                case "--anthropic":
                case "-a":
                    System.out.println("Using Anthropic Claude model");
                    return createChatModel(ModelType.ANTHROPIC_CLAUDE);
                    
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
                String modelName = (i + 2 < args.length) ? args[i + 2] : "granite-code:20b";
                System.out.println("Using Ollama at " + baseUrl + " with model: " + modelName);
                return ChatModels.createOllamaModel(baseUrl, modelName);
            }
        }
        
        // Default: use Granite code model (best for planning)
        System.out.println("Using default Granite model (granite-code:20b)");
        return createChatModel(ModelType.GRANITE_CODE);
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
                    System.out.println("Using Granite planning model (granite-code:20b)");
                    return createChatModel(ModelType.GRANITE_CODE);
                    
                case "--anthropic":
                case "-a":
                    System.out.println("Using Anthropic Claude planning model");
                    return createChatModel(ModelType.ANTHROPIC_CLAUDE);
                    
                case "--auto":
                    System.out.println("Auto-selecting planning model from environment");
                    return ChatModels.createFromEnvironment();
            }
        }
        
        // Check for model-specific arguments (reuse single model logic)
        return createSingleModelFromArgs(args);
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
                case "--granite3-moe":
                    System.out.println("Using Granite3 MoE code generation model (granite3-moe:3b)");
                    return createChatModel(ModelType.GRANITE3_MOE);
                    
                case "--granite":
                case "-g":
                    System.out.println("Using Granite3 MoE code generation model (granite3-moe:3b)");
                    return createChatModel(ModelType.GRANITE3_MOE);
                    
                case "--qwen":
                case "--qwen-coder":
                    System.out.println("Using Qwen2.5 Coder code generation model (qwen2.5-coder:14b)");
                    return createChatModel(ModelType.QWEN_CODER);
                    
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
                String modelName = (i + 2 < args.length) ? args[i + 2] : "qwen2.5-coder:14b";
                System.out.println("Using Ollama at " + baseUrl + " for code generation with model: " + modelName);
                return ChatModels.createOllamaModel(baseUrl, modelName);
            }
        }
        
        // Default: use Qwen2.5 Coder model (fast and supports tools)
        System.out.println("Using default Qwen2.5 Coder code generation model (qwen2.5-coder:14b)");
        return createChatModel(ModelType.QWEN_CODER);
    }

    /**
     * Gets a model recommendation based on use case.
     * 
     * @param useCase The intended use case
     * @return Recommended ModelType
     */
    public static ModelType getRecommendedModel(UseCase useCase) {
        return switch (useCase) {
            case PLANNING -> ModelType.GRANITE_CODE;        // Best for coordination
            case CODE_GENERATION -> ModelType.QWEN_CODER;   // Fast with tools
            case CLOUD_QUALITY -> ModelType.ANTHROPIC_CLAUDE; // High quality
            case LIGHTWEIGHT -> ModelType.GRANITE3_MOE;     // Resource efficient
        };
    }

    /**
     * Use cases for model selection.
     */
    public enum UseCase {
        PLANNING,         // High-level planning and coordination
        CODE_GENERATION,  // Fast DRL code generation with tools
        CLOUD_QUALITY,    // High-quality cloud-based generation
        LIGHTWEIGHT       // Resource-constrained environments
    }
}