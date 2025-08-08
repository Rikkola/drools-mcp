package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.registry.FactTypeRegistry;
import org.drools.agentic.example.registry.InMemoryFactTypeRegistry;

/**
 * Hybrid loop-based DRL authoring workflow that combines AI agents with deterministic services.
 * This replaces the single-agent approach with a guaranteed working DRL generator using
 * LangChain4j's native loop capabilities with optimized non-LLM validation.
 * 
 * The loop continues until both validation and execution succeed, or maximum iterations are reached.
 * 
 * AI Agent Architecture:
 * 1. DRLGeneratorAgent (AI) - Generates/refines DRL code based on requirements and feedback
 * 2. DRLValidatorAgent (AI with Tools) - Uses tool-based validation for DRL syntax and structure
 * 3. DRLExecutorAgent (AI with Tools) - Executes DRL with test data to verify runtime behavior
 * 
 * Benefits of Tool-Based Approach:
 * - Deterministic validation through tools (consistent parser-based results)
 * - AI agents can interpret and respond to validation results intelligently  
 * - Better error handling and feedback interpretation
 * - Unified agent pattern across all workflow steps
 * 
 * State Management:
 * - current_drl: The generated DRL code
 * - validation_feedback: Detailed validation issues to fix
 * - execution_feedback: Detailed runtime issues to fix
 */
public class DRLAuthoringLoop {

    /**
     * Creates a loop-based DRL authoring workflow with iterative validation and execution.
     * Uses AI agents with tool-based validation for better result interpretation.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @param registry The fact type registry for managing fact type definitions
     * @param maxIterations Maximum number of loop iterations (default: 3)
     * @return A configured loop-based DRL authoring agent
     */
    public static UntypedAgent create(ChatModel chatModel, FactTypeRegistry registry, int maxIterations) {


        // Create individual specialized agents - all using AI with tools
        DRLGeneratorAgent generatorAgent = DRLGeneratorAgent.create(chatModel, registry);
        DRLValidatorAgent validatorAgent = DRLValidatorAgent.create(chatModel);
        DRLExecutorAgent executorAgent = DRLExecutorAgent.create(chatModel);

        return AgenticServices.loopBuilder()
                .subAgents(generatorAgent, validatorAgent, executorAgent)
                .maxIterations(maxIterations)
                .exitCondition(cognisphere -> {
                    // Continue loop until both validation and execution succeed
                    boolean isValid = cognisphere.readState("validation_feedback", "not empty").isEmpty();
                    boolean executionSuccessful = cognisphere.readState("execution_feedback", "not empty").isEmpty();
                    return isValid && executionSuccessful;
                })
                .build();
    }

    /**
     * Creates a loop-based DRL authoring workflow with default settings.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @param registry The fact type registry for managing fact type definitions
     * @return A configured loop-based DRL authoring agent with 3 max iterations
     */
    public static UntypedAgent create(ChatModel chatModel, FactTypeRegistry registry) {
        return create(chatModel, registry, 3);
    }

    /**
     * Creates a loop-based DRL authoring workflow with empty registry.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @return A configured loop-based DRL authoring agent with empty registry
     */
    public static UntypedAgent create(ChatModel chatModel) {
        return create(chatModel, new InMemoryFactTypeRegistry());
    }

}
