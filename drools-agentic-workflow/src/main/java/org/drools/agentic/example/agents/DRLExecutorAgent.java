package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
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

        1. READ: Get "current_drl" from cognisphere state
        2. CHECK: Only execute if "drl_valid" is true
        3. EXECUTE: Use executeDRLWithFacts tool with appropriate test data
        4. ASSESS: Determine if execution completed without errors
        5. STORE: Save execution results to cognisphere:
           - "execution_successful": true/false
           - "execution_feedback": runtime issues to fix

        EXECUTION PROCESS:
        - Generate appropriate JSON test facts based on DRL declared types
        - Ensure '_type' field matches declared type names exactly
        - Use realistic test data that will trigger the rules
        - Handle edge cases and boundary conditions in test data

        TEST DATA FORMAT:
        [{"_type":"TypeName", "field1":"value1", "field2":"value2"}]

        EXECUTION ASSESSMENT:
        - Success: DRL compiles and executes without exceptions
        - Failure: Compilation errors, runtime exceptions, or unexpected behavior

        If execution succeeds, set execution_successful=true.
        If execution fails, provide specific feedback about runtime issues.

        CRITICAL: Always save execution results:
        - cognisphere.writeState("execution_successful", true/false)
        - cognisphere.writeState("execution_feedback", "detailed feedback")
        """)
    @UserMessage("Execute the current validated DRL code from cognisphere state")
    @Agent("DRL code executor for loop workflow")
    String executeDRL(@V("cognisphere") Object cognisphere);

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
                .tools(executionService)
                .build();
    }
}