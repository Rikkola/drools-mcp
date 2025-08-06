package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsDRLAuthoringAgent;

public class DroolsAgent {


    /**
     * Creates and returns a DroolsSupervisorAgent that coordinates all Drools agents.
     * The wrapper provides enhanced prompting and error handling for DRL tasks.
     * 
     * @param planningModel The chat model to use for the supervisor/planning agent
     * @param codeGenModel The chat model to use for code generation agents
     * @return A configured DroolsSupervisorAgent ready for use
     */
    public static DroolsSupervisorAgent createDroolsSupervisorAgent(ChatModel planningModel, ChatModel codeGenModel) {
        // Create individual specialized agents with the code generation model
        DroolsDRLAuthoringAgent authoringAgent = DroolsDRLAuthoringAgent.create(codeGenModel);

        // Build the base supervisor agent that coordinates the specialized agents
        SupervisorAgent baseSupervisor = AgenticServices.supervisorBuilder()
                .chatModel(planningModel)  // Use planning model for supervisor
                .subAgents(authoringAgent)
                .build();
        
        // Wrap it with Drools optimization for better model performance
        return DroolsSupervisorAgent.create(baseSupervisor);
    }

    /**
     * Convenience method that uses the same model for both planning and code generation.
     * 
     * @param chatModel The chat model to use for all agents
     * @return A configured DroolsSupervisorAgent ready for use
     */
    public static DroolsSupervisorAgent createDroolsSupervisorAgent(ChatModel chatModel) {
        return createDroolsSupervisorAgent(chatModel, chatModel);
    }
}
