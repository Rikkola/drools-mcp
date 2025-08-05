package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.agents.DroolsDRLAuthoringAgent;

public class DroolsAgent {


    /**
     * Creates and returns a SupervisorAgent that coordinates all Drools agents.
     * 
     * @param chatModel The chat model to use for all agents
     * @return A configured SupervisorAgent ready for use
     */
    public static SupervisorAgent createDroolsSupervisorAgent(ChatModel chatModel) {
        // Create individual specialized agents
        DroolsDRLAuthoringAgent authoringAgent = DroolsDRLAuthoringAgent.create(chatModel);

        // Build and return supervisor agent that coordinates the specialized agents
        return AgenticServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(authoringAgent)
                .build();
    }
}
