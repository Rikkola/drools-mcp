package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TestJSONAuthoringAgent {

    @SystemMessage("""
            You are a test JSON author for DRL code. Your job is to:
            
            1. RECEIVE: DRL code
            2. CHECK: Assess if you can create test data for the DRL code. If not,
                return a report about issues.
            5. RETURN: JSON objects for a test run
            
            EXECUTION PROCESS:
            - Generate appropriate JSON test facts based on DRL declared types
            - Ensure '_type' field matches declared type names exactly
            - Create realistic test data that will trigger the rules
            
            CRITICAL JSON FACT TYPE MATCHING:
            - The '_type' field in JSON MUST match the simple class name from DRL declare statements
            - If DRL has: "declare User", use "_type":"User" (NOT "_type":"com.package.User")
            - If DRL has: "declare Person", use "_type":"Person" (NOT "_type":"com.example.Person")
            - Package names in DRL are for compilation only, JSON facts use simple names
            
            TEST DATA FORMAT:
            [{"_type":"TypeName", "field1":"value1", "field2":"value2"}]
            
            EXAMPLES:
            ✅ CORRECT: DRL "declare User" → JSON {"_type":"User", "age":25}
            ❌ WRONG: DRL "declare User" → JSON {"_type":"com.example.User", "age":25}
            
            """)
    @UserMessage("""
                Create test JSON objects for this DRL code: {{current_drl}}.
                If not empty, here is the feedback from the previous set you made and the issues with it: {{execution_feedback}}.
            """)
    @Agent(value = "JSON test data authoring",
            outputName = "test_json")
    String generateJSON(@MemoryId String memoryId,
                        @V("current_drl") String currentDrl,
                        @V("execution_feedback") String executionFeedback);

    static TestJSONAuthoringAgent create(ChatModel chatModel) {

        return AgenticServices.agentBuilder(TestJSONAuthoringAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15))
                .build();
    }
}
