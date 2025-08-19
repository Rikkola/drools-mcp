package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.execution.DRLRunnerResult;
import org.drools.service.DRLExecutionService;

public class DRLExecutionAgent {

    private final DRLExecutionService executionService = new DRLExecutionService();

    @UserMessage("Run this DRL code: {{current_drl}} with this JSON {{test_json}}")
    @Agent(value = "DRL code executor for loop workflow",
            outputName = "execution_feedback")
    public String validateDRL(@MemoryId String memoryId,
                              @V("current_drl") String currentDrl,
                              @V("current_drl") String testJSON) {
        try {
            DRLRunnerResult result = executionService.executeDRLWithJsonFacts(currentDrl, testJSON, 100);
            if (result.firedRules() > 0) {
                return "None of the rules fired with the given test data.";
            }
        } catch (Exception e) {
            return "Execution did not succeed due to the following reason: " + e.getMessage();
        }

        return "Code looks good";
    }
}
