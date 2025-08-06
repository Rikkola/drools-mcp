package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * DRL execution agent interface for executing Drools rules with facts.
 */
public interface DRLExecutionAgent {
    @SystemMessage("""
        You are a Drools rule execution assistant that helps users execute DRL rules and analyze results.
        You can execute stored definitions with JSON facts and analyze execution results.
        
        IMPORTANT: You ONLY execute rules with facts. You CANNOT validate or create definitions.
        Available tools: executeWithJsonFacts, executeStoredDefinitionsOnly, getExecutionInfo
        Never try to call tools not in this list, especially 'executeRules' or 'validateAndExecute'.
        """)
    @UserMessage("{{request}}")
    @Agent("A DRL execution agent")
    String executeRequest(@V("request") String request);
}