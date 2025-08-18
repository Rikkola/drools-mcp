package org.drools.agentic.example.main;

import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsWorkflowOrchestratorAgent;
import org.drools.agentic.example.agents.EnhancedDroolsWorkflowOrchestratorAgent;
import org.drools.agentic.example.workflow.WorkflowConfiguration;
import org.drools.agentic.example.workflow.WorkflowResult;

/**
 * Enhanced example main class demonstrating advanced Drools workflow orchestration.
 * 
 * This class creates a sequential workflow with comprehensive monitoring, error handling,
 * progress tracking, and performance analytics.
 */
public class DroolsWorkflowOrchestratorExampleMain {

    public static void main(String[] args) {
        // Print available models if --help is requested
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            ModelSelector.printAvailableModels();
            return;
        }
        
        // Parse command line arguments for workflow options
        WorkflowConfiguration config = parseWorkflowConfig(args);
        
        // Choose models using ModelSelector
        ChatModel planningModel = ModelSelector.createPlanningModelFromArgs(args);
        ChatModel codeGenModel = ModelSelector.createCodeGenModelFromArgs(args);
        
        System.out.println("🤖 Using planning model: " + planningModel.getClass().getSimpleName());
        System.out.println("🤖 Using code generation model: " + codeGenModel.getClass().getSimpleName());
        System.out.println("⚙️  Workflow configuration: " + config);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🚀 ENHANCED DROOLS WORKFLOW ORCHESTRATION DEMO");
        System.out.println("=".repeat(70));
        
        try {
            // Create enhanced workflow with monitoring and error handling
            var enhancedWorkflow = EnhancedDroolsWorkflowOrchestratorAgent.create(
                planningModel, codeGenModel, config);
            
            // Execute workflow with comprehensive monitoring
            String result = enhancedWorkflow.author("I would like to find out if my users are adults or not.");
            
            // Display results
            System.out.println("\n✅ WORKFLOW COMPLETED SUCCESSFULLY!");
            System.out.println("📄 Final Result:");
            if (result != null) {
                if (result.length() > 1000) {
                    System.out.println(result.substring(0, 1000) + "...");
                    System.out.printf("   (Total length: %d characters)%n", result.length());
                } else {
                    System.out.println(result);
                }
            }
            
            // Also demonstrate original workflow for comparison if requested
            if (shouldShowComparison(args)) {
                demonstrateOriginalWorkflow(planningModel, codeGenModel);
            }
            
        } catch (Exception e) {
            System.err.println("\n❌ WORKFLOW EXECUTION FAILED!");
            System.err.printf("Error: %s%n", e.getMessage());
            
            if (config.isEnableDetailedLogging()) {
                System.err.println("Stack trace:");
                e.printStackTrace();
            }
            
            System.exit(1);
        }
    }
    
    private static WorkflowConfiguration parseWorkflowConfig(String[] args) {
        WorkflowConfiguration config = WorkflowConfiguration.development(); // Default
        
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "--production":
                    config = WorkflowConfiguration.production();
                    break;
                case "--quiet":
                    config = WorkflowConfiguration.quiet();
                    break;
                case "--no-progress":
                    config.setEnableProgressBar(false);
                    config.setEnableProgressReporting(false);
                    break;
                case "--no-metrics":
                    config.setEnableMetrics(false);
                    break;
                case "--no-retry":
                    config.setEnableRetry(false);
                    break;
                case "--verbose":
                    config.setLogLevel(WorkflowConfiguration.LogLevel.DEBUG);
                    config.setEnableDetailedLogging(true);
                    break;
            }
        }
        
        return config;
    }
    
    private static boolean shouldShowComparison(String[] args) {
        for (String arg : args) {
            if ("--compare".equals(arg)) {
                return true;
            }
        }
        return false;
    }
    
    private static void demonstrateOriginalWorkflow(ChatModel planningModel, ChatModel codeGenModel) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 COMPARISON: ORIGINAL WORKFLOW");
        System.out.println("=".repeat(70));
        
        try {
            var originalWorkflow = DroolsWorkflowOrchestratorAgent.create(planningModel, codeGenModel);
            
            long startTime = System.currentTimeMillis();
            Object result = originalWorkflow.author("I would like to find out if my users are adults or not.");
            long duration = System.currentTimeMillis() - startTime;
            
            System.out.println("⏱️  Original workflow completed in: " + duration + "ms");
            System.out.println("📄 Original workflow result type: " + result.getClass().getName());
            
            if (result instanceof java.util.Map) {
                java.util.Map<?, ?> mapResult = (java.util.Map<?, ?>) result;
                System.out.println("📋 Original workflow map contents:");
                for (java.util.Map.Entry<?, ?> entry : mapResult.entrySet()) {
                    System.out.println("   " + entry.getKey() + " -> " + 
                        (entry.getValue().toString().length() > 100 ? 
                         entry.getValue().toString().substring(0, 100) + "..." : 
                         entry.getValue().toString()));
                }
            } else {
                String resultStr = result.toString();
                if (resultStr.length() > 200) {
                    System.out.println("📄 Original result: " + resultStr.substring(0, 200) + "...");
                } else {
                    System.out.println("📄 Original result: " + resultStr);
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Original workflow failed: " + e.getMessage());
        }
    }
}
