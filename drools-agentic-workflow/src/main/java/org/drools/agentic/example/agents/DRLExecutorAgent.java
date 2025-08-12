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

        1. RECEIVE: DRL code
        2. CHECK: Assess if you can create test data for the DRL code. If not, 
            return a report about issues. 
        3. EXECUTE: Use executeDRLWithFacts tool with appropriate test data
        4. ASSESS: Determine if execution completed without errors
        5. RETURN: Execution feedback for the loop workflow
          - Success: DRL compiles and executes without exceptions → return "Code looks good"
          - Failure: Compilation errors, runtime exceptions, or unexpected behavior → return error details

        EXECUTION PROCESS:
        - Generate appropriate JSON test facts based on DRL declared types
        - Ensure '_type' field matches declared type names exactly
        - Use realistic test data that will trigger the rules
        - Handle edge cases and boundary conditions in test data

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

        CRITICAL: Your response determines loop continuation:
        - String "Code looks good" = execution successful, can exit loop
        - Non-empty string = execution step failed, continue loop with feedback
        """)
    @UserMessage("Execute this DRL code: {{current_drl}} and return a report on found issues.")
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
