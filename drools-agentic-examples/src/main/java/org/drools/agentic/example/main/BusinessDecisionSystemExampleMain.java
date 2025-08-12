package org.drools.agentic.example.main;

import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsWorkflowOrchestratorAgent;
import dev.langchain4j.agentic.UntypedAgent;
import java.util.Map;

/**
 * Example demonstrating creation of a comprehensive business decision system.
 * 
 * This example showcases the creation of a sophisticated rule-based decision system
 * for handling complex business scenarios with multiple entity types, validation rules,
 * calculations, and decision pathways. The system demonstrates advanced rule authoring
 * capabilities for real-world business applications.
 */
public class BusinessDecisionSystemExampleMain {

    public static void main(String[] args) {
        // Print available models if --help is requested
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            ModelSelector.printAvailableModels();
            return;
        }
        
        // Choose models using ModelSelector
        ChatModel planningModel = ModelSelector.createPlanningModelFromArgs(args);
        ChatModel codeGenModel = ModelSelector.createCodeGenModelFromArgs(args);
        
        System.out.println("Using planning model: " + planningModel.getClass().getSimpleName());
        System.out.println("Using code generation model: " + codeGenModel.getClass().getSimpleName());

        // Create comprehensive business decision system
        System.out.println("=== Business Decision System Creation Demo ===");
        var agentWorkflow = DroolsWorkflowOrchestratorAgent.create(planningModel, codeGenModel);
        
        String businessRequest = """
            Create a comprehensive business decision system for an e-commerce platform that handles:
            
            1. Customer Management:
               - Customer entity with: id, name, email, loyaltyLevel (BRONZE/SILVER/GOLD/PLATINUM), 
                 registrationDate, totalPurchases, isActive
               - Automatic loyalty level upgrades based on purchase amounts
               - Customer validation rules (email format, required fields)
            
            2. Product Management:
               - Product entity with: id, name, category, basePrice, stockQuantity, isDiscountEligible, seasonalFlag
               - Inventory management rules (low stock alerts, out-of-stock handling)
               - Category-based pricing adjustments
            
            3. Order Processing:
               - Order entity with: id, customerId, items (list), orderDate, totalAmount, status, shippingMethod
               - OrderItem entity with: productId, quantity, unitPrice, subtotal
               - Complex pricing calculations including discounts, taxes, and shipping
               - Business validation rules for order processing
            
            4. Discount and Promotion Engine:
               - Loyalty-based discounts (5% BRONZE, 10% SILVER, 15% GOLD, 20% PLATINUM)
               - Seasonal promotions (20% off winter items in December)
               - Bulk purchase discounts (10% off orders over $500, 15% off over $1000)
               - First-time customer bonus (25% off first order)
            
            5. Business Rules:
               - Free shipping for orders over $100 or GOLD+ customers
               - Express shipping discounts for PLATINUM customers (50% off)
               - Maximum order quantity limits per product (prevent bulk buying abuse)
               - Regional tax calculations based on shipping location
               - Return eligibility rules (within 30 days, certain categories excluded)
            
            The system should include comprehensive data validation, complex business logic,
            mathematical calculations, conditional processing, and decision trees.
            Please create a robust rule-based system that demonstrates enterprise-level
            business decision automation capabilities.
            """;
        
        Map<String, Object> input = Map.of(
            "request", businessRequest
        );
        
        Object result = agentWorkflow.author(businessRequest);
        System.out.println("Business Decision System Creation Result:");
        System.out.println("=========================================");
        System.out.println(result);
        System.out.println("=========================================");
        System.out.println("✅ Complex business decision system has been successfully generated!");
        System.out.println("📋 The system includes multiple entity types, validation rules, calculations, and decision logic.");
        System.out.println("🏪 This demonstrates enterprise-grade business automation capabilities.");
    }
}