package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
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

        1. READ: Get "current_drl" from cognisphere state
        2. VALIDATE: Use validateDRLStructure tool to check the DRL syntax and structure
        3. ASSESS: Determine if DRL is syntactically valid based on tool results
        4. STORE: Save validation results to cognisphere:
           - "drl_valid": true/false
           - "validation_feedback": detailed issues to fix

        VALIDATION PROCESS:
        - Extract DRL code from cognisphere.readState("current_drl")
        - Call validateDRLStructure with the DRL code
        - Parse tool response to determine if validation passed or failed
        - Look for "VALIDATION PASSED" or "VALIDATION FAILED" in results

        RESULT PARSING:
        - If tool response contains "VALIDATION PASSED" → set drl_valid=true
        - If tool response contains "VALIDATION FAILED" → set drl_valid=false
        - Extract error messages from tool response for validation_feedback

        If you need help understanding DRL syntax, use getValidationGuidance tool.

        CRITICAL: Always save validation results:
        - cognisphere.writeState("drl_valid", true/false)
        - cognisphere.writeState("validation_feedback", "detailed feedback")
        """)
    @UserMessage("Validate the current DRL code from cognisphere state")
    @Agent("DRL code validator for loop workflow")
    String validateDRL(@V("cognisphere") Object cognisphere);

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
                .tools(validationService)
                .build();
    }

}