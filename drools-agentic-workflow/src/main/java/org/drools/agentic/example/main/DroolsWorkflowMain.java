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
        
        // Choose single model for both planning and code generation (chat model = planning model)
        ChatModel model = ModelSelector.createSingleModelFromArgs(args);
        
        System.out.println("Using model for both planning and code generation: " + model.getClass().getSimpleName());

        // Create the supervisor agent using DroolsService factory method with same model
        DroolsSupervisor droolsSupervisorAgent = DroolsService.createDroolsSupervisorAgent(model, model);

        // Example 1: Use supervisor agent for complete workflow
        System.out.println("=== Supervisor Agent Demo ===");
        String result1 = droolsSupervisorAgent.invoke("""
            Create a Person type with name, age, and adult fields. Then create rules that check if a person is an adult or not.
            """);
        System.out.println("Supervisor Result:");
        System.out.println(result1);

    }

}
