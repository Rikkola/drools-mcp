package org.drools.agentic.example.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;

/**
 * Utility class for creating and managing ChatModel instances.
 * Provides pre-configured chat models for use with Drools agents.
 * 
 * <h3>Supported Models:</h3>
 * <ul>
 *   <li><strong>Anthropic Claude:</strong> Cloud-based, requires API key</li>
 *   <li><strong>Local Ollama:</strong> Self-hosted, privacy-focused, no API key needed</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>
 * // Use default models
 * ChatModel anthropic = ChatModels.DEFAULT_ANTHROPIC_MODEL;
 * ChatModel ollama = ChatModels.DEFAULT_OLLAMA_MODEL;
 * 
 * // Create custom Ollama model
 * ChatModel custom = ChatModels.createOllamaModel("llama3.2:8b");
 *
 * // Auto-select based on environment
 * ChatModel auto = ChatModels.createFromEnvironment();
 * </pre>
 * 
 * <h3>Environment Variables:</h3>
 * <ul>
 *   <li><code>ANTHROPIC_API_KEY</code> - Enables Anthropic models</li>
 *   <li><code>OLLAMA_MODEL</code> - Specifies Ollama model name</li>
 *   <li><code>OLLAMA_BASE_URL</code> - Custom Ollama server URL</li>
 * </ul>
 * 
 * <h3>Command Line Usage:</h3>
 * <pre>
 * java DroolsWorkflowMain                   # Use default Granite model (granite-code:20b)
 * java DroolsWorkflowMain --granite         # Use Granite code model (default)
 * java DroolsWorkflowMain --ollama          # Use default Ollama (llama3.2:3b)
 * java DroolsWorkflowMain --ollama-8b       # Use Llama 8B model
 * java DroolsWorkflowMain --anthropic       # Use Anthropic Claude
 * java DroolsWorkflowMain --auto            # Auto-select from environment
 * java DroolsWorkflowMain --ollama-model mistral:7b   # Custom model
 * java DroolsWorkflowMain --ollama-url http://remote:11434 llama3.2:3b
 * </pre>
 */
public class ChatModels {

    // Default connection settings for Ollama
    private static final String DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(2);
    
    /**
     * Default Anthropic Claude chat model configured for Drools agents.
     * Uses claude-3-haiku-20240307 with request/response logging enabled.
     */
    public static final ChatModel DEFAULT_ANTHROPIC_MODEL = AnthropicChatModel.builder()
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName("claude-3-haiku-20240307")
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * Default local Ollama chat model configured for Drools agents.
     * Uses granite-code:8b model optimized for DRL code generation.
     */
    public static final ChatModel DEFAULT_OLLAMA_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("granite-code:8b")
            .timeout(Duration.ofMinutes(5))
            .temperature(0.1)        // Low temperature for consistent code generation
            .topP(0.9)              // Focus on most likely tokens
            .numPredict(1024)       // Reasonable response length for DRL
            .numCtx(8192)           // Context window for code understanding
            .repeatPenalty(1.1)     // Reduce repetitive output
            .logRequests(true)
            .logResponses(true)
            .build();
    
    /**
     * Planning model using granite-code:20b.
     * IBM's Granite Code model optimized for planning and code generation.
     */
    public static final ChatModel OLLAMA_GRANITE_PLANNING_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("granite-code:20b")
            .timeout(Duration.ofMinutes(5))
            .temperature(0.2)        // Slightly higher for creative planning
            .topP(0.95)             // More diverse planning options
            .numPredict(2048)       // Longer responses for detailed plans
            .numCtx(16384)          // Large context for complex planning
            .repeatPenalty(1.05)    // Light penalty for planning variety
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * Code generation model using granite-code:20b.
     * IBM's Granite Code model optimized for code generation tasks.
     */
    public static final ChatModel OLLAMA_GRANITE_CODE_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("granite-code:20b")
            .timeout(Duration.ofMinutes(5))
            .temperature(0.05)       // Very low for precise code generation
            .topP(0.85)             // Focus on most reliable code patterns
            .numPredict(1536)       // Medium length for code blocks
            .numCtx(12288)          // Large context for code understanding
            .repeatPenalty(1.15)    // Higher penalty for repetitive code
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * Tool-calling model using qwen3:14b.
     * Qwen3 model optimized for function calling and tool usage.
     */
    public static final ChatModel OLLAMA_QWEN_TOOL_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("qwen3:14b")
            .timeout(Duration.ofMinutes(5))
            .temperature(0.0)        // Deterministic for tool calling
            .topP(0.8)              // Focused token selection for tools
            .numPredict(800)        // Shorter responses for tool calls
            .numCtx(8192)           // Standard context for tool usage
            .repeatPenalty(1.2)     // Strong penalty for repetitive calls
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * High-performance local Ollama model using granite3-moe:3b.
     * IBM's Granite3 MoE model optimized for code generation and tool usage.
     * Supports tools and excellent for DRL authoring tasks.
     */
    public static final ChatModel OLLAMA_GRANITE3_MOE_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("granite3-moe:3b")
            .timeout(Duration.ofMinutes(3))
            .temperature(0.1)        // Low temperature for consistent output
            .topP(0.9)              // Balanced token selection
            .numPredict(1200)       // Medium responses for fast turnaround
            .numCtx(6144)           // Moderate context for 3B model efficiency
            .repeatPenalty(1.1)     // Light repetition penalty
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ChatModels() {
        throw new UnsupportedOperationException("ChatModels is a utility class and should not be instantiated");
    }

    /**
     * Creates a new Anthropic Claude chat model with custom configuration.
     * 
     * @param modelName The name of the model to use (e.g., "claude-3-haiku-20240307")
     * @param logRequests Whether to log requests
     * @param logResponses Whether to log responses
     * @return A configured AnthropicChatModel
     */
    public static ChatModel createAnthropicModel(String modelName, boolean logRequests, boolean logResponses) {
        return AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * Creates a new Anthropic Claude chat model with default logging settings.
     * 
     * @param modelName The name of the model to use
     * @return A configured AnthropicChatModel with logging enabled
     */
    public static ChatModel createAnthropicModel(String modelName) {
        return createAnthropicModel(modelName, true, true);
    }

    /**
     * Creates a new Ollama chat model with custom configuration.
     * 
     * @param baseUrl The Ollama server URL (e.g., "http://localhost:11434")
     * @param modelName The name of the model to use (e.g., "llama3.2:3b", "codellama:13b")
     * @param timeout Timeout duration for requests
     * @param logRequests Whether to log requests
     * @param logResponses Whether to log responses
     * @return A configured OllamaChatModel
     */
    public static ChatModel createOllamaModel(String baseUrl, String modelName, Duration timeout, 
                                             boolean logRequests, boolean logResponses) {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(timeout)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * Creates a new Ollama chat model with default settings (localhost, default timeout, logging enabled).
     * 
     * @param modelName The name of the model to use (e.g., "llama3.2:3b", "codellama:13b")
     * @return A configured OllamaChatModel with default settings
     */
    public static ChatModel createOllamaModel(String modelName) {
        return createOllamaModel(DEFAULT_OLLAMA_BASE_URL, modelName, DEFAULT_TIMEOUT, true, true);
    }

    /**
     * Creates a new Ollama chat model with custom base URL and default settings.
     * 
     * @param baseUrl The Ollama server URL (e.g., "http://remote-server:11434")
     * @param modelName The name of the model to use
     * @return A configured OllamaChatModel
     */
    public static ChatModel createOllamaModel(String baseUrl, String modelName) {
        return createOllamaModel(baseUrl, modelName, DEFAULT_TIMEOUT, true, true);
    }

    /**
     * Gets the default tool-calling model (qwen3:14b).
     * Use this for agents that need function calling capabilities.
     * 
     * @return ChatModel optimized for tool calling
     */
    public static ChatModel getToolCallingModel() {
        return OLLAMA_QWEN_TOOL_MODEL;
    }

    /**
     * Creates a model based on environment configuration.
     * Checks environment variables to determine which model to use:
     * - If ANTHROPIC_API_KEY is set, uses Anthropic model
     * - If OLLAMA_MODEL is set, uses that Ollama model  
     * - If OLLAMA_BASE_URL is set, uses that URL
     * - Otherwise, falls back to DEFAULT_OLLAMA_MODEL
     * 
     * @return A configured ChatModel based on environment
     */
    public static ChatModel createFromEnvironment() {
        String anthropicKey = System.getenv("ANTHROPIC_API_KEY");
        String ollamaModel = System.getenv("OLLAMA_MODEL");
        String ollamaBaseUrl = System.getenv("OLLAMA_BASE_URL");
        
        if (anthropicKey != null && !anthropicKey.trim().isEmpty()) {
            return DEFAULT_ANTHROPIC_MODEL;
        }
        
        if (ollamaModel != null && !ollamaModel.trim().isEmpty()) {
            String baseUrl = (ollamaBaseUrl != null && !ollamaBaseUrl.trim().isEmpty()) 
                ? ollamaBaseUrl : DEFAULT_OLLAMA_BASE_URL;
            return createOllamaModel(baseUrl, ollamaModel);
        }
        
        // Default to local Ollama if available
        return DEFAULT_OLLAMA_MODEL;
    }
}