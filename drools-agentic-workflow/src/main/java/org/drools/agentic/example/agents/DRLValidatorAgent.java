package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.services.validation.DRLValidatorToolService;

/**
 * DRL code validator agent for the loop-based workflow.
 * Focuses solely on validating DRL code syntax and structure.
 */
public interface DRLValidatorAgent {

    @SystemMessage("""
        You are a DRL code validator. Your job is to:

        1. VALIDATE: DRL code provided via @V("current_drl") parameter
        2. PROCESS: Use validateDRLStructure tool to check syntax and structure  
        3. ASSESS: Determine if DRL is syntactically valid based on tool results
        4. RETURN: Provide validation feedback or success confirmation

        VALIDATION PROCESS:
        - DRL code is provided via @V("current_drl") parameter
        - Call validateDRLStructure with the DRL code
        - Parse tool response to determine if validation passed or failed
        - Look for "VALIDATION PASSED" or "VALIDATION FAILED" in results
        - Return structured feedback about validation status

        RESULT PARSING:
        - If tool response contains "VALIDATION PASSED" → return success message
        - If tool response contains "VALIDATION FAILED" → return detailed error feedback
        - Extract and provide specific error messages for fixing

        If you need help understanding DRL syntax, use getValidationGuidance tool.

        RETURN FORMAT:
        Return a validation result message that indicates success or provides specific feedback for fixing issues.
        """)
    @UserMessage("Validate this DRL code: {{current_drl}}")
    @Agent("DRL code validator for loop workflow")
    String validateDRL(@MemoryId String memoryId, @V("current_drl") String currentDrl);

    /**
     * Creates a DRLValidatorAgent with validation tools.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @return A configured DRL validator agent
     */
    static DRLValidatorAgent create(ChatModel chatModel) {
        DRLValidatorToolService validationService = new DRLValidatorToolService();

        return AgenticServices.agentBuilder(DRLValidatorAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15))
                .outputName("validation_feedback")
                .tools(validationService)
                .build();
    }

}
