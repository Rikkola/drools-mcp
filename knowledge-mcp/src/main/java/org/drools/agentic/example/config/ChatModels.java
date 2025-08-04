package org.drools.agentic.example.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;

/**
 * Utility class for creating and managing ChatModel instances.
 * Provides pre-configured chat models for use with Drools agents.
 */
public class ChatModels {

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
}