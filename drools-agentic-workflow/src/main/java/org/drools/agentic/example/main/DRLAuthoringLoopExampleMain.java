package org.drools.agentic.example.main;

import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.drools.agentic.example.agents.DRLAuthoringAgent;
import org.drools.agentic.example.registry.FactTypeRegistry;
import org.drools.agentic.example.registry.InMemoryFactTypeRegistry;

/**
 * Example demonstrating the loop-based DRL authoring workflow.
 * 
 * This example shows how the new loop-based approach provides guaranteed working DRL
 * by iteratively refining the code through validation and execution cycles.
 */
public class DRLAuthoringLoopExampleMain {

    public static void main(String[] args) {
        // Configure the chat model (requires OpenAI API key)
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4")
                .temperature(0.7)
                .build();

        // Create a fact type registry (can be pre-populated)
        FactTypeRegistry registry = new InMemoryFactTypeRegistry();

        // Create the loop-based DRL authoring agent
        UntypedAgent loopBasedAgent = DRLAuthoringAgent.createLoopBasedAgent(chatModel, registry, 3);

        // Example 1: Simple customer order rules
        System.out.println("=== Example 1: Customer Order Rules ===");
        String request1 = """
            Create DRL rules for an e-commerce system that:
            1. Applies a 10% discount for orders over $100
            2. Provides free shipping for orders over $50
            3. Marks VIP customers with total orders over $1000
            
            Use fact types: Order (id, amount, customerId) and Customer (id, name, totalOrders)
            """;
        
        String result1 = loopBasedAgent.execute(request1);
        System.out.println("Result: " + result1);
        System.out.println();

        // Example 2: Insurance claim processing
        System.out.println("=== Example 2: Insurance Claim Rules ===");
        String request2 = """
            Create DRL rules for insurance claim processing that:
            1. Auto-approves claims under $500 for customers with good history
            2. Flags suspicious claims with unusual patterns
            3. Calculates deductibles based on policy type
            
            Use fact types: Claim (id, amount, type, customerId) and Policy (id, type, deductible)
            """;
        
        String result2 = loopBasedAgent.execute(request2);
        System.out.println("Result: " + result2);
        System.out.println();

        // Example 3: Complex business rules with multiple conditions
        System.out.println("=== Example 3: Complex Business Rules ===");
        String request3 = """
            Create DRL rules for a loan approval system that:
            1. Requires manual review for loans over $50,000
            2. Auto-approves loans for customers with credit score > 750 and income > $60,000
            3. Applies risk-based interest rates based on credit score ranges
            4. Requires co-signer for applicants under 21 years old
            
            Use appropriate fact types for loan applications, customer data, and credit information.
            """;
        
        String result3 = loopBasedAgent.execute(request3);
        System.out.println("Result: " + result3);
    }
}