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
 * ChatModel remote = ChatModels.createOllamaModel("http://server:11434", "codellama:13b");
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
 * java DroolsWorkflowMain --codellama       # Use CodeLlama model
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
     * Uses llama3.2:3b model with default localhost connection.
     */
    public static final ChatModel DEFAULT_OLLAMA_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("llama3.2:3b")
            .timeout(DEFAULT_TIMEOUT)
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * High-performance local Ollama model using llama3.2:8b.
     * Better reasoning capabilities but requires more memory.
     */
    public static final ChatModel OLLAMA_LLAMA_8B_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("llama3.2:8b")
            .timeout(DEFAULT_TIMEOUT)
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * Code-focused local Ollama model using codellama:13b.
     * Optimized for code generation and analysis tasks.
     */
    public static final ChatModel OLLAMA_CODELLAMA_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("codellama:13b")
            .timeout(Duration.ofMinutes(3)) // Longer timeout for complex code tasks
            .logRequests(true)
            .logResponses(true)
            .build();

    /**
     * High-performance local Ollama model using granite-code:20b.
     * IBM's Granite code model optimized for planning and coordination tasks.
     * Good for supervisor agents but doesn't support tools.
     */
    public static final ChatModel OLLAMA_GRANITE_MODEL = OllamaChatModel.builder()
            .baseUrl(DEFAULT_OLLAMA_BASE_URL)
            .modelName("granite-code:20b")
            .timeout(Duration.ofMinutes(5)) // Longer timeout for large model
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