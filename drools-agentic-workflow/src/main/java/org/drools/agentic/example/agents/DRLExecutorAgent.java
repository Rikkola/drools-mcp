package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.services.execution.SimpleDRLExecutionToolService;

/**
 * DRL code executor agent for the loop-based workflow.
 * Focuses solely on executing DRL code with appropriate test data.
 */
public interface DRLExecutorAgent {

    @SystemMessage("""
        You are a DRL code executor. Your job is to:

        1. RECEIVE: DRL code provided via @V("current_drl") parameter
        2. CHECK: Only proceed if validation status indicates DRL is valid  
        3. EXECUTE: Use executeDRLWithFacts tool with appropriate test data
        4. ASSESS: Determine if execution completed without errors
        5. RETURN: Execution feedback for the loop workflow
           - SUCCESS: Return empty string "" (no runtime issues found)
           - FAILURE: Return specific runtime issues that need to be fixed

        EXECUTION PROCESS:
        - Generate appropriate JSON test facts based on DRL declared types
        - Ensure '_type' field matches declared type names exactly
        - Use realistic test data that will trigger the rules
        - Handle edge cases and boundary conditions in test data

        TEST DATA FORMAT:
        [{"_type":"TypeName", "field1":"value1", "field2":"value2"}]

        EXECUTION ASSESSMENT:
        - Success: DRL compiles and executes without exceptions → return "Code looks good"
        - Failure: Compilation errors, runtime exceptions, or unexpected behavior → return error details

        CRITICAL: Your response determines loop continuation:
        - Empty string "" = execution successful, can exit loop
        - Non-empty string = execution failed, continue loop with feedback
        """)
    @UserMessage("Execute this DRL code: {{current_drl}}")
    @Agent("DRL code executor for loop workflow")
    String executeDRL(@MemoryId String memoryId, @V("current_drl") String currentDrl);

    /**
     * Creates a DRLExecutorAgent with execution tools.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @return A configured DRL executor agent
     */
    static DRLExecutorAgent create(ChatModel chatModel) {
        SimpleDRLExecutionToolService executionService = new SimpleDRLExecutionToolService();

        return AgenticServices.agentBuilder(DRLExecutorAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15))
                .outputName("execution_feedback")
                .tools(executionService)
                .build();
    }
}
