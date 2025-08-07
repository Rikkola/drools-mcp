package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.services.authoring.SimpleDRLValidationToolService;

/**
 * DRL code validator agent for the loop-based workflow.
 * Focuses solely on validating DRL code syntax and structure.
 */
public interface DRLValidatorAgent {

    @SystemMessage("""
        You are a DRL code validator. Your job is to:

        1. READ: Get "current_drl" from cognisphere state
        2. VALIDATE: Use validateWithGuidance tool to check the DRL
        3. ASSESS: Determine if DRL is syntactically valid
        4. STORE: Save validation results to cognisphere:
           - "drl_valid": true/false
           - "validation_feedback": detailed issues to fix

        VALIDATION CRITERIA:
        - Proper package declaration
        - Correct declare block syntax
        - Valid rule syntax
        - Field type compatibility
        - Proper DRL formatting

        If validation passes, set drl_valid=true. If fails, provide specific feedback.
        Be thorough but concise in your feedback - focus on actionable items to fix.

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
        SimpleDRLValidationToolService validationService = new SimpleDRLValidationToolService();

        return AgenticServices.agentBuilder(DRLValidatorAgent.class)
                .chatModel(chatModel)
                .tools(validationService)
                .build();
    }
}