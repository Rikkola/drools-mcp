package org.drools.agentic.example.examples;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.ArrayList;
import java.util.List;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import org.drools.agentic.example.config.ChatModels;

/**
 * Decision service powered by Anthropic AI.
 * This service provides methods to make decisions based on provided context and constraints.
 * When a decision is incomplete, additional information and constraints can be provided
 * to reach a better, more informed decision.
 */
public class AnthropicTool {

    private final ChatModel model;
    private final List<String> decisionHistory;
    private final List<String> constraints;

    public AnthropicTool() {
        this.model = ChatModels.createFromEnvironment();
        this.decisionHistory = new ArrayList<>();
        this.constraints = new ArrayList<>();
    }

    public AnthropicTool(ChatModel model) {
        this.model = model;
        this.decisionHistory = new ArrayList<>();
        this.constraints = new ArrayList<>();
    }

    /**
     * @param problem The problem or decision that needs to be made
     * @return The decision response including confidence level and reasoning
     */
    @Tool(description = "Makes a decision based on the problem description and context,")
    public String makeDecision(@ToolArg(description = "The problem description.") String problem) {
        try {
            String systemPrompt = buildDecisionSystemPrompt();
            String decisionPrompt = String.format(
                "Problem to decide: %s\n\nPlease provide:\n1. Your decision\n2. Confidence level (1-10)\n3. Reasoning\n4. What additional information would improve this decision",
                problem
            );
            
            ChatResponse response = model.chat(
                SystemMessage.from(systemPrompt),
                UserMessage.from(decisionPrompt)
            );
            
            String decision = response.aiMessage().text();
            decisionHistory.add("Problem: " + problem + " | Decision: " + decision);
            
            return formatResponse("success", decision, null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * This method uses the decision history to build upon previous decisions.
     * @param additionalInfo Additional information to consider
     * @param newConstraints New constraints to apply to the decision
     * @return The refined decision response
     */
    @Tool(description = "Refines a previous decision by providing additional information and constraint.")
    public String refineDecision( @ToolArg(description = "Addintional information.") String additionalInfo, 
                                  @ToolArg(description = "New constraints for decision making.") String newConstraints) {
        try {
            if (newConstraints != null && !newConstraints.trim().isEmpty()) {
                constraints.add(newConstraints);
            }
            
            String systemPrompt = buildDecisionSystemPrompt();
            String refinementPrompt = String.format(
                "Previous decision context:\n%s\n\nAdditional information: %s\n\nPlease refine the decision considering this new information and provide:\n1. Updated decision\n2. Updated confidence level (1-10)\n3. How this new information changed your reasoning\n4. Any remaining uncertainties",
                getDecisionContext(),
                additionalInfo
            );
            
            ChatResponse response = model.chat(
                SystemMessage.from(systemPrompt),
                UserMessage.from(refinementPrompt)
            );
            
            String refinedDecision = response.aiMessage().text();
            decisionHistory.add("Refinement with info: " + additionalInfo + " | Decision: " + refinedDecision);
            
            return formatResponse("success", refinedDecision, null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * @param constraint The constraint to add to the decision-making process
     * @return JSON-formatted confirmation
     */
    @Tool(description = "Add a constraint that will be considered in the future decisions.")
    public String addConstraint( @ToolArg(description = "New constraints for decision making.") String constraint) {
        try {
            constraints.add(constraint);
            return formatResponse("success", "Constraint added: " + constraint + ". Total constraints: " + constraints.size(), null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * @return JSON-formatted string with decision context
     */
    @Tool(description = "Gives the current decision history and active constraints.")
    public String getDecisionStatus() {
        try {
            String status = String.format("Decision History: %d entries | Active Constraints: %d | Model: claude-3-haiku-20240307",
                decisionHistory.size(), constraints.size());
            return formatResponse("success", status, null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * @return JSON-formatted confirmation
     */
    @Tool(description = "Clears the decision history and constraints to start fresh.")
    public String resetDecisionContext() {
        try {
            int historySize = decisionHistory.size();
            int constraintsSize = constraints.size();
            decisionHistory.clear();
            constraints.clear();
            return formatResponse("success", String.format("Reset complete. Cleared %d decisions and %d constraints", historySize, constraintsSize), null);
        } catch (Exception e) {
            return formatResponse("error", null, e.getMessage());
        }
    }

    /**
     * Builds the system prompt for decision-making including current constraints.
     * @return The system prompt for decision-making
     */
    private String buildDecisionSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a decision-making AI assistant. Your role is to analyze problems and provide well-reasoned decisions. ");
        prompt.append("Always provide structured responses with: decision, confidence level (1-10), reasoning, and gaps in information. ");
        
        if (!constraints.isEmpty()) {
            prompt.append("Current constraints to consider: ");
            for (int i = 0; i < constraints.size(); i++) {
                prompt.append((i + 1)).append(". ").append(constraints.get(i));
                if (i < constraints.size() - 1) prompt.append(" ");
            }
        }
        
        return prompt.toString();
    }

    /**
     * Gets the current decision context including history and constraints.
     * @return String representation of the decision context
     */
    private String getDecisionContext() {
        StringBuilder context = new StringBuilder();
        
        if (!decisionHistory.isEmpty()) {
            context.append("Recent decisions:\n");
            int start = Math.max(0, decisionHistory.size() - 3);
            for (int i = start; i < decisionHistory.size(); i++) {
                context.append("- ").append(decisionHistory.get(i)).append("\n");
            }
        }
        
        if (!constraints.isEmpty()) {
            context.append("Active constraints:\n");
            for (int i = 0; i < constraints.size(); i++) {
                context.append("- ").append(constraints.get(i)).append("\n");
            }
        }
        
        return context.toString();
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
