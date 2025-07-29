import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * Tool provider class for Anthropic AI operations.
 * This class provides methods to interact with Anthropic's Claude models.
 */
public class AnthropicTool {

    private final ChatModel model;

    public AnthropicTool() {
        this.model = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-3-haiku-20240307")
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    public AnthropicTool(ChatModel model) {
        this.model = model;
    }

    /**
     * Sends a simple chat message to the Anthropic model and returns the response.
     * @param message The message to send to the model
     * @return The model's response as a string
     */
    public String chat(String message) {
        try {
            String response = model.chat(message);
            return formatResponse("success", response, null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * Sends a message with system context to the Anthropic model.
     * @param systemMessage The system message to provide context
     * @param userMessage The user's message
     * @return The model's response as a JSON-formatted string
     */
    public String chatWithSystem(String systemMessage, String userMessage) {
        try {
            ChatResponse response = model.chat(
                SystemMessage.from(systemMessage),
                UserMessage.from(userMessage)
            );
            String content = response.aiMessage().text();
            return formatResponse("success", content, null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * Gets information about the current model configuration.
     * @return JSON-formatted string with model information
     */
    public String getModelInfo() {
        try {
            String modelInfo = "Model: claude-3-haiku-20240307, API Key: " + 
                (System.getenv("ANTHROPIC_API_KEY") != null ? "configured" : "not configured");
            return formatResponse("success", modelInfo, null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * Tests the connection to the Anthropic API.
     * @return JSON-formatted string indicating success or failure
     */
    public String testConnection() {
        try {
            String response = model.chat("Hello");
            return formatResponse("success", "Connection successful. Response: " + response, null);
        } catch (Exception e) {
            return formatResponse("error", null, "Connection failed: " + e.getMessage());
        }
    }

    /**
     * Formats the response in a consistent JSON-like format similar to DRLTool.
     * @param status The status of the operation (success/error)
     * @param content The content to include in the response
     * @param error The error message if any
     * @return Formatted JSON-like string
     */
    private String formatResponse(String status, String content, String error) {
        StringBuilder response = new StringBuilder();
        response.append("{");
        response.append("\"status\":\"").append(status).append("\"");
        
        if (content != null) {
            response.append(",\"content\":\"").append(escapeJson(content)).append("\"");
        }
        
        if (error != null) {
            response.append(",\"error\":\"").append(escapeJson(error)).append("\"");
        }
        
        response.append(",\"timestamp\":\"").append(System.currentTimeMillis()).append("\"");
        response.append("}");
        
        return response.toString();
    }

    /**
     * Escapes JSON special characters in a string.
     * @param input The input string to escape
     * @return The escaped string
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}