package dev.langchain4j.agentic.example;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgentServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.model.anthropic.AnthropicChatModel;

public class DroolsAgent {

    // Define the Drools definition management agent
    public interface DroolsDefinitionAgent {
        @SystemMessage("""
            You are a Drools definition management assistant that helps users manage their DRL definitions.
            You can store, retrieve, organize, and generate DRL code from declared types, functions, globals, and other Drools definitions.
            Always provide helpful responses and use the available tools to perform the requested operations.
            """)
        @UserMessage("{{request}}")
        @Agent("A Drools definition management agent")
        String handleRequest(@V("request") String request);
    }


    public static void main(String[] args) {
        ChatModel chatModel = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-3-haiku-20240307")
                .logRequests(true)
                .logResponses(true)
                .build();

        // Create service instances
        DefinitionStorageService definitionService = new DefinitionStorageService();

        // Build the Drools definition management agent
        DroolsDefinitionAgent droolsAgent = AgentServices.agentBuilder(DroolsDefinitionAgent.class)
                .chatModel(chatModel)
                .tools(definitionService)
                .build();

        // Example usage - natural language requests to manage Drools definitions
        String result1 = droolsAgent.handleRequest(
            "Add a Person type definition with name, age, and email fields, then create a function to validate age range"
        );
        System.out.println("Result 1:");
        System.out.println(result1);

        String result2 = droolsAgent.handleRequest("Show me all the definitions I have stored");
        System.out.println("\nResult 2:");
        System.out.println(result2);

        String result3 = droolsAgent.handleRequest("Generate complete DRL code with package name com.example.rules");
        System.out.println("\nResult 3:");
        System.out.println(result3);

        // Direct service calls for demonstration
        System.out.println("\n--- Direct Service Calls ---");
        
        definitionService.addDefinition("Order", "declare", "declare Order\n    id: String\n    amount: double\n    customer: String\nend");
        definitionService.addDefinition("calculateDiscount", "function", "function double calculateDiscount(double amount) {\n    return amount > 100 ? amount * 0.1 : 0.0;\n}");
        
        System.out.println("Definitions Summary:");
        System.out.println(definitionService.getDefinitionsSummary());
    }
}
