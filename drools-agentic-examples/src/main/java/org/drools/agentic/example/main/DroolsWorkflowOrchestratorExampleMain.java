package org.drools.agentic.example.main;

import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsWorkflowOrchestratorAgent;
import dev.langchain4j.agentic.UntypedAgent;
import java.util.Map;

/**
 * Example main class demonstrating Drools workflow orchestration.
 * 
 * This class creates a sequential workflow of DRL authoring, file storage, and knowledge base agents
 * to demonstrate complete end-to-end DRL development workflow automation.
 */
public class DroolsWorkflowOrchestratorExampleMain {

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

        // Example: Create agent workflow and invoke with demo request
        System.out.println("=== Drools Workflow Orchestration Demo ===");
        var agentWorkflow = DroolsWorkflowOrchestratorAgent.create(planningModel, codeGenModel);
        
        Map<String, Object> input = Map.of(
            "request", "Create a simple Person DRL rule with fields name, age, and adult, then save it to a file called person-rules.drl"
        );
        
        Object result = agentWorkflow.invoke(input);
        System.out.println("Workflow Orchestration Result:");
        System.out.println(result);
    }
}
