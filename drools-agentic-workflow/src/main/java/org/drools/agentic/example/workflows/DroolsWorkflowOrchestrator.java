package org.drools.agentic.example.workflows;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicTokenUsage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.orchestration.DroolsService;
import org.drools.agentic.example.agents.FileStorageAgent;
import org.drools.agentic.example.agents.DRLAuthoringAgent;
import org.drools.agentic.example.agents.DroolsKnowledgeBaseAgent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;

public class DroolsWorkflowOrchestrator {


    public UntypedAgent createAgentWorkflow(ChatModel planningModel, ChatModel codeGenModel) {
        // Use the factory method that includes tools
        var droolsAuthoringAgent = DRLAuthoringAgent.create(codeGenModel);
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
