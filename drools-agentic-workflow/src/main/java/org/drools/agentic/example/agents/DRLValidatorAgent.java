package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.service.DRLValidationService;

/**
 * DRL code validator agent for the loop-based workflow.
 * Focuses solely on validating DRL code syntax and structure.
 */
public class DRLValidatorAgent {

    private final DRLValidationService validationService = new DRLValidationService();

    @UserMessage("Validate this DRL code: {{current_drl}}")
    @Agent(value = "DRL code validator for loop workflow",
            outputName = "validation_feedback")
    public String validateDRL(@MemoryId String memoryId, @V("current_drl") String currentDrl){
      var result = validationService.validateDRLStructure(currentDrl);
      return result;
    }
}
