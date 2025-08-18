package org.drools.agentic.example.agents;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.config.ChatModels;

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
public interface DroolsWorkflowOrchestratorAgent {

    @UserMessage("""
      I will take in {{textInput}} and create a knowledge base out of it.
      Steps:
      1. Create DRL out of the {{textInput}}
      2. Save the DRL to file FileStorageAgent
      3. Compile a knowledge base from stored files
    """)
    @Agent("Workflow agent.")
    String author(@V("textInput") String textInput);

    /**
     * Creates a sequential agent workflow for DRL development.
     * 
     * @param planningModel ChatModel for planning and coordination tasks
     * @param codeGenModel ChatModel for code generation tasks
     * @return Configured UntypedAgent workflow
     */
    public static DroolsWorkflowOrchestratorAgent create(ChatModel planningModel, ChatModel codeGenModel) {
        // Use the factory method with dual models - planning model for orchestration, code gen model for implementation
        var droolsAuthoringAgent = DRLAuthoringAgent.create(planningModel, codeGenModel);
        var fileStorageAgent = FileStorageAgent.create(ChatModels.getToolCallingModel());
        var knowledgeBaseAgent = DroolsKnowledgeBaseAgent.create(ChatModels.getToolCallingModel());

        DroolsWorkflowOrchestratorAgent agentWorkflow = AgenticServices
                .sequenceBuilder(DroolsWorkflowOrchestratorAgent.class)
                .subAgents(droolsAuthoringAgent, fileStorageAgent, knowledgeBaseAgent)
                .outputName("result")
                .build();

        return agentWorkflow;
    }

}
