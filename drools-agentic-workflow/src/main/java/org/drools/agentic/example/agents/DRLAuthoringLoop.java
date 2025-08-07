package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.drools.agentic.example.registry.FactTypeRegistry;
import org.drools.agentic.example.registry.InMemoryFactTypeRegistry;
import org.drools.agentic.example.services.validation.DRLValidatorService;

/**
 * Hybrid loop-based DRL authoring workflow that combines AI agents with deterministic services.
 * This replaces the single-agent approach with a guaranteed working DRL generator using
 * LangChain4j's native loop capabilities with optimized non-LLM validation.
 * 
 * The loop continues until both validation and execution succeed, or maximum iterations are reached.
 * 
 * Hybrid Architecture:
 * 1. DRLGeneratorAgent (AI) - Generates/refines DRL code based on requirements and feedback
 * 2. DRLValidatorService (Non-AI) - Fast deterministic DRL syntax and structure validation
 * 3. DRLExecutorAgent (AI) - Executes DRL with test data to verify runtime behavior
 * 
 * Benefits of Hybrid Approach:
 * - Faster validation (no LLM calls for deterministic syntax checking)
 * - More reliable validation (consistent parser-based results)
 * - Cost efficient (reduced LLM usage)
 * - AI creativity where needed (generation and execution strategy)
 * 
 * State Management:
 * - current_drl: The generated DRL code
 * - drl_valid: Boolean indicating if DRL passed validation
 * - validation_feedback: Detailed validation issues to fix
 * - execution_successful: Boolean indicating if DRL executed successfully
 * - execution_feedback: Detailed runtime issues to fix
 */
public class DRLAuthoringLoop {

    /**
     * Creates a loop-based DRL authoring workflow with iterative validation and execution.
     * Uses a non-LLM validator service for faster, more reliable validation.
     * 
     * @param chatModel The chat model to use for generation and execution agents (must support tools)
     * @param registry The fact type registry for managing fact type definitions
     * @param maxIterations Maximum number of loop iterations (default: 3)
     * @return A configured loop-based DRL authoring agent
     */
    public static UntypedAgent create(ChatModel chatModel, FactTypeRegistry registry, int maxIterations) {
        // Create individual specialized agents
        DRLGeneratorAgent generatorAgent = DRLGeneratorAgent.create(chatModel, registry);
        DRLValidatorService validatorService = new DRLValidatorService(); // Non-LLM validator
        DRLExecutorAgent executorAgent = DRLExecutorAgent.create(chatModel);

        return AgenticServices.loopBuilder()
                .subAgents(generatorAgent, validatorService, executorAgent)
                .maxIterations(maxIterations)
                .exitCondition(cognisphere -> {
                    // Continue loop until both validation and execution succeed
                    boolean isValid = cognisphere.readState("drl_valid", false);
                    boolean executionSuccessful = cognisphere.readState("execution_successful", false);
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

    /**
     * Creates a loop-based DRL authoring workflow with custom iteration limit and empty registry.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @param maxIterations Maximum number of loop iterations
     * @return A configured loop-based DRL authoring agent
     */
    public static UntypedAgent createWithMaxIterations(ChatModel chatModel, int maxIterations) {
        return create(chatModel, new InMemoryFactTypeRegistry(), maxIterations);
    }
}