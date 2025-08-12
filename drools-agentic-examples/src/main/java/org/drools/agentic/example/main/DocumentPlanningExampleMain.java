package org.drools.agentic.example.main;

import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DRLAuthoringAgent;

/**
 * Example main class demonstrating Document Planning Agent capabilities.
 * 
 * This class showcases the DocumentPlanningAgent's ability to analyze any text input
 * and extract business knowledge that can be used for rule base creation.
 * The agent is technology-agnostic and focuses on business logic rather than implementation.
 */
public class DocumentPlanningExampleMain {

    public static void main(String[] args) {
        // Print available models if --help is requested
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            ModelSelector.printAvailableModels();
            System.out.println("\nDocument Planning Agent Usage:");
            System.out.println("This agent analyzes text and extracts:");
            System.out.println("- Domain Models & Entities");
            System.out.println("- Business Rules & Logic");
            System.out.println("- Decision Points & Criteria");
            System.out.println("- Business Processes & Workflows");
            return;
        }
        
        // Choose analysis model using ModelSelector (use planning model for reasoning)
        ChatModel analysisModel = ModelSelector.createPlanningModelFromArgs(args);
        
        System.out.println("Using analysis model: " + analysisModel.getClass().getSimpleName());

        // Create DocumentPlanningAgent
        System.out.println("=== Document Planning Agent Demo ===");
        var documentAgent = DRLAuthoringAgent.createDocumentPlanningAgent(analysisModel);
        
        // Example business text for analysis
        String businessText = getExampleBusinessText(args);
        
        System.out.println("\n--- Input Business Text ---");
        System.out.println(businessText);
        System.out.println("\n--- Analysis Output ---");
        
        // Analyze the text and extract business knowledge
        String analysisResult = documentAgent.analyzeDomainFromText(businessText);
        System.out.println(analysisResult);
        
        System.out.println("\n=== Document Analysis Completed ===");
    }
    
    /**
     * Returns example business text for analysis.
     * If custom text is provided via args, uses that instead.
     */
    private static String getExampleBusinessText(String[] args) {
        // Check if custom text is provided as argument
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--text")) {
                return args[i + 1];
            }
        }
        
        // Default example: E-commerce order processing
        return """
            **E-Commerce Order Processing System**
            
            Our online store handles customer orders with the following business process:
            
            When a customer places an order, we first check if they are a registered member. 
            Premium members get a 10% discount on all orders. If the customer is new, 
            we create a customer profile with their basic information including name, 
            email, shipping address, and phone number.
            
            Each order contains multiple items with quantities and prices. We calculate 
            the subtotal by multiplying item price by quantity for each item. If the 
            order total exceeds $100, we offer free shipping. Orders under $50 require 
            a minimum $5 shipping fee.
            
            For payment processing, we accept credit cards and PayPal. If payment fails, 
            we hold the order for 24 hours before cancellation. Successful payments 
            trigger inventory checks - if any item is out of stock, we backorder it 
            and notify the customer about expected delivery dates.
            
            Orders are assigned priorities: Express (1-2 days), Standard (3-5 days), 
            or Economy (7-10 days). Express orders require signature confirmation. 
            We also track order status: Pending, Processing, Shipped, Delivered, or Cancelled.
            
            Customer service rules apply: orders can be modified within 2 hours of placement, 
            cancelled within 24 hours if not shipped, and returns are accepted within 30 days 
            of delivery for non-perishable items.
            """;
    }
}