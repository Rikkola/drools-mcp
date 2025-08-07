package org.drools.agentic.example.main;

import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.workflows.DroolsWorkflowOrchestrator;
import java.util.Map;

public class DroolsWorkflowOrchestratorRunner {

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

        // Create the DroolsWorkflowOrchestrator instance
        DroolsWorkflowOrchestrator droolsWorkflowOrchestrator = new DroolsWorkflowOrchestrator();
        
        // Example: Create agent workflow and invoke with demo request
        System.out.println("=== DroolsWorkflowOrchestrator Demo ===");
        var agentWorkflow = droolsWorkflowOrchestrator.createAgentWorkflow(planningModel, codeGenModel);
        
        Map<String, Object> input = Map.of(
            "request", "Create a simple Person DRL rule with fields name, age, and adult, then save it to a file called person-rules.drl"
        );
        
        Object result = agentWorkflow.invoke(input);
        System.out.println("DroolsWorkflowOrchestrator Result:");
        System.out.println(result);
    }

}