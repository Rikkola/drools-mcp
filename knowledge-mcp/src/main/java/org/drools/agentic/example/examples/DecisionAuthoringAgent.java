package org.drools.agentic.example.examples;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicTokenUsage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.drools.agentic.example.config.ChatModels;

public class DecisionAuthoringAgent {

    ChatModel model = ChatModels.createFromEnvironment();

    public String requests () {
        String answer = model.chat("What is the capital of Germany?");

        return answer;
    }
}
