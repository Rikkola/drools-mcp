package org.drools.agentic.example.examples;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicTokenUsage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.agents.DroolsAgent;
import org.drools.agentic.example.agents.FileStorageAgentInterface;
import org.drools.agentic.example.agents.DroolsDRLAuthoringAgent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;

public class MainAgent {


    public UntypedAgent createAgentWorkflow(ChatModel planningModel, ChatModel codeGenModel) {
        // Use the factory method that includes tools
        var droolsAuthoringAgent = DroolsDRLAuthoringAgent.create(codeGenModel);
        var fileStorageAgent = FileStorageAgentInterface.create(codeGenModel);

        UntypedAgent agentWorkflow = AgenticServices
                .sequenceBuilder()
                .subAgents(droolsAuthoringAgent, fileStorageAgent)
                .outputName("result")
                .build();

        return agentWorkflow;
    }
}
