package org.drools.agentic.example.main;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.orchestration.DroolsService;
import org.drools.agentic.example.orchestration.DroolsSupervisor;
import org.drools.agentic.example.agents.DRLExecutionAgent;
import org.drools.agentic.example.services.execution.DRLExecutionToolService;
import org.drools.agentic.example.services.authoring.DRLValidationToolService;
import org.drools.storage.DefinitionStorage;

public class DroolsWorkflowMain {

    public static void main(String[] args) {
        // Print available models if --help is requested
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            ModelSelector.printAvailableModels();
            return;
        }
        
        // Use dual model approach: granite-code:20b for planning, qwen2.5-coder:14b for code generation
        System.out.println("🎯 Creating planning model...");
        ChatModel planningModel = ModelSelector.createPlanningModelFromArgs(args);
        System.out.println("🔧 Creating code generation model...");
        ChatModel codeGenModel = ModelSelector.createCodeGenModelFromArgs(args);
        
        System.out.println("✅ Models configured successfully!");

        // Create the supervisor agent using DroolsService factory method with dual models
        DroolsSupervisor droolsSupervisorAgent = DroolsService.createDroolsSupervisorAgent(planningModel, codeGenModel);

        // Example 1: Use supervisor agent for complete workflow
        System.out.println("=== Supervisor Agent Demo ===");
        String result1 = droolsSupervisorAgent.invoke("""
            Create a Person type with name, age, and adult fields. Then create rules that check if a person is an adult or not.
            """);
        System.out.println("Supervisor Result:");
        System.out.println(result1);

    }

}
