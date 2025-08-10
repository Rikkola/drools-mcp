package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.registry.FactTypeRegistry;
import org.drools.agentic.example.registry.InMemoryFactTypeRegistry;
import org.drools.agentic.example.config.ChatModels;

/**
 * Drools DRL authoring orchestration agent that coordinates the DRL development workflow.
 * 
 * This agent provides two main workflows:
 * 1. PLANNING + LOOP WORKFLOW: Uses planning model for coordination and code gen model for implementation
 * 2. LOOP-ONLY WORKFLOW: Direct loop-based DRL generation with iterative validation and execution
 * 
 * Loop-Based Features:
 * - Iterative validation and execution until both succeed or max iterations reached
 * - AI agents with tool-based validation for deterministic results
 * - Memory-enabled agents that learn from previous iterations
 * - Context summarization between agents for improved collaboration
 * - Automatic termination when validation and execution both succeed
 * 
 * Memory and Context Features:
 * - Each agent maintains conversation memory via @MemoryId parameters
 * - MessageWindowChatMemory preserves context across iterations
 * - Summarized context sharing between agents enables learning from previous steps
 * - Memory allows agents to build on prior attempts and avoid repeating mistakes
 * 
 * State Management:
 * - current_drl: The generated DRL code
 * - validation_feedback: Detailed validation issues to fix
 * - execution_feedback: Detailed runtime issues to fix
 */
public class DRLAuthoringAgent {

    /**
     * Planning agent interface for analyzing requirements and coordinating workflow.
     */
    public interface DRLPlanningAgent {
        @SystemMessage("""
            You are a DRL workflow planning and coordination agent. Your responsibilities are:
            
            1. ANALYZE the user request to understand DRL requirements
            2. PLAN the development approach and identify needed fact types
            3. COORDINATE with the code generation workflow
            4. ENSURE quality by reviewing final outputs
            
            PLANNING WORKFLOW:
            1. Break down the user request into specific requirements
            2. Identify what fact types and rules are needed
            3. Plan the validation and testing strategy
            4. Delegate to the loop-based generation workflow
            5. Review and approve the final DRL output
            
            Keep your analysis concise and focused on high-level planning.
            The actual code generation will be handled by specialized sub-agents.
            """)
        @UserMessage("Plan DRL development for: {{request}}")
        @Agent("DRL workflow planning coordinator")
        String planDRLWorkflow(@V("request") String request);
    }

    // ========== PUBLIC API METHODS ==========

    /**
     * Creates a DRLAuthoringAgent with planning and loop-based generation workflow.
     * Uses sequenceBuilder to coordinate between planning and generation phases.
     * 
     * @param planningModel The chat model to use for planning (should be good at reasoning)
     * @param codeGenModel The chat model to use for code generation (should be good at tools)
     * @param registry The fact type registry to use (can be pre-loaded with existing types)
     * @return A configured orchestration agent with planning and generation workflow
     */
    public static UntypedAgent create(ChatModel planningModel, ChatModel codeGenModel, FactTypeRegistry registry) {
        // Create planning agent for high-level coordination
        DRLPlanningAgent planningAgent = AgenticServices.agentBuilder(DRLPlanningAgent.class)
                .chatModel(planningModel)
                .build();
        
        // Create loop-based generation workflow for actual DRL creation
        UntypedAgent loopGenerationWorkflow = createLoopWorkflow(codeGenModel, registry, 3);
        
        // Sequence: Planning → Loop-based Generation
        return AgenticServices.sequenceBuilder()
                .subAgents(planningAgent, loopGenerationWorkflow)
                .outputName("drl_output")
                .build();
    }

    /**
     * Creates a loop-based DRL authoring workflow with iterative validation and execution.
     * Uses AI agents with tool-based validation and memory for better result interpretation.
     * 
     * Features:
     * - Stateful agents with conversation memory to learn from previous iterations
     * - Context summarization between agents for improved collaboration
     * - Tool-based validation and execution for deterministic results
     * - Automatic termination when both validation and execution succeed
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @param registry The fact type registry for managing fact type definitions
     * @param maxIterations Maximum number of loop iterations (default: 3)
     * @return A configured loop-based DRL authoring agent with memory support
     */
    public static UntypedAgent createLoopWorkflow(ChatModel chatModel, FactTypeRegistry registry, int maxIterations) {
        // Create individual specialized agents - using different models based on capabilities
        DRLGeneratorAgent generatorAgent = DRLGeneratorAgent.create(chatModel, registry);
        DRLValidatorAgent validatorAgent = DRLValidatorAgent.create(ChatModels.getToolCallingModel());
        DRLExecutorAgent executorAgent = DRLExecutorAgent.create(ChatModels.getToolCallingModel());

        return AgenticServices.loopBuilder()
                .subAgents(generatorAgent, validatorAgent, executorAgent)
                .maxIterations(maxIterations)
                .exitCondition(cognisphere -> {
                    // Continue loop until both validation and execution succeed
                    boolean isValid = cognisphere.readState("validation_feedback", "not empty").isEmpty();
                    boolean executionSuccessful = cognisphere.readState("execution_feedback", "not empty").isEmpty();
                    return isValid && executionSuccessful;
                })
                .outputName("current_drl")
                .build();
    }

    // ========== CONVENIENCE METHODS ==========

    /**
     * Creates a loop-based DRL authoring workflow with default settings.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @param registry The fact type registry for managing fact type definitions
     * @return A configured loop-based DRL authoring agent with 3 max iterations
     */
    public static UntypedAgent createLoopWorkflow(ChatModel chatModel, FactTypeRegistry registry) {
        return createLoopWorkflow(chatModel, registry, 3);
    }

    /**
     * Creates a loop-based DRL authoring workflow with empty registry.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @return A configured loop-based DRL authoring agent with empty registry
     */
    public static UntypedAgent createLoopWorkflow(ChatModel chatModel) {
        return createLoopWorkflow(chatModel, new InMemoryFactTypeRegistry());
    }

}