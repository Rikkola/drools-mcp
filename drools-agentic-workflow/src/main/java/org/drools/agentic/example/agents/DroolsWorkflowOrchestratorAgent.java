package org.drools.agentic.example.agents;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import org.drools.agentic.example.registry.InMemoryFactTypeRegistry;

/**
 * Agent that orchestrates a sequential workflow of DRL development agents.
 * 
 * This agent creates a workflow that chains together:
 * 1. DRLAuthoringAgent - Creates DRL rules based on specifications
 * 2. FileStorageAgent - Saves the generated DRL to files
 * 3. DroolsKnowledgeBaseAgent - Builds and validates the knowledge base
 * 
 * The sequential workflow ensures each step completes before the next begins,
 * providing a reliable end-to-end DRL development automation pipeline.
 */
public class DroolsWorkflowOrchestratorAgent {

    /**
     * Creates a sequential agent workflow for DRL development.
     * 
     * @param planningModel ChatModel for planning and coordination tasks
     * @param codeGenModel ChatModel for code generation tasks
     * @return Configured UntypedAgent workflow
     */
    public static UntypedAgent create(ChatModel planningModel, ChatModel codeGenModel) {
        // Use the factory method with dual models - planning model for orchestration, code gen model for implementation
        var droolsAuthoringAgent = DRLAuthoringAgent.create(planningModel, codeGenModel, new InMemoryFactTypeRegistry());
        var fileStorageAgent = FileStorageAgent.create(codeGenModel);
        var knowledgeBaseAgent = DroolsKnowledgeBaseAgent.create(codeGenModel);

        UntypedAgent agentWorkflow = AgenticServices
                .sequenceBuilder()
                .subAgents(droolsAuthoringAgent, fileStorageAgent, knowledgeBaseAgent)
                .outputName("result")
                .build();

        return agentWorkflow;
    }

}