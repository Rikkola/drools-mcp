package org.drools.agentic.example.main;

import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.FileStorageAgent;

/**
 * Simple test to verify FileStorageAgent can handle raw DRL content.
 */
public class FileStorageTestMain {

    public static void main(String[] args) {
        // Use local model for testing
        ChatModel chatModel = ModelSelector.createCodeGenModelFromArgs(args);
        
        System.out.println("=== FileStorageAgent DRL Auto-Detection Test ===");
        
        // Create FileStorageAgent
        FileStorageAgent fileStorageAgent = FileStorageAgent.create(chatModel);
        
        // Test with sample DRL content (simulating output from DRLAuthoringAgent)
        String sampleDRL = """
            package com.example.ecommerce.rules
            
            declare Customer
                id : String
                name : String
                loyaltyLevel : String
            end
            
            declare Product
                id : String
                name : String
                basePrice : double
            end
            
            rule "Loyalty Upgrade"
            when
                $customer : Customer(loyaltyLevel == "BRONZE")
            then
                modify($customer) {
                    setLoyaltyLevel("SILVER")
                }
            end
            
            rule "Product Discount"
            when
                $product : Product(basePrice > 100)
            then
                modify($product) {
                    setBasePrice($product.basePrice * 0.9)
                }
            end
            """;
        
        System.out.println("Testing FileStorageAgent with raw DRL content...");
        System.out.println("DRL Content Length: " + sampleDRL.length() + " characters");
        
        try {
            String result = fileStorageAgent.handleRequest(sampleDRL);
            System.out.println("FileStorageAgent Result:");
            System.out.println("=======================");
            System.out.println(result);
            System.out.println("=======================");
            System.out.println("✅ File storage test completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error during file storage test:");
            e.printStackTrace();
        }
    }
}