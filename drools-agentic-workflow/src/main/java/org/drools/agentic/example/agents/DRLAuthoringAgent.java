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
import org.drools.agentic.example.services.execution.SimpleDRLExecutionToolService;
import org.drools.agentic.example.services.authoring.SimpleDRLValidationToolService;
import org.drools.agentic.example.services.registry.FactTypeRegistryToolService;

/**
 * Drools DRL authoring orchestration agent that coordinates the DRL development workflow.
 * This agent acts as a planner/coordinator using the planning model to break down requirements
 * and delegate actual code generation to the loop-based DRL generation workflow.
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
        UntypedAgent loopGenerationWorkflow = DRLAuthoringLoop.create(codeGenModel, registry);
        
        // Sequence: Planning → Loop-based Generation
        return AgenticServices.sequenceBuilder()
                .subAgents(planningAgent, loopGenerationWorkflow)
                .outputName("drl_output")
                .build();
    }

    /**
     * Creates a DRLAuthoringAgent using the same model for both planning and code generation.
     * This is the single-model convenience method.
     * 
     * @param chatModel The chat model to use for both planning and generation
     * @param registry The fact type registry to use
     * @return A configured DRLAuthoringAgent with planning and generation workflow
     */
    public static UntypedAgent create(ChatModel chatModel, FactTypeRegistry registry) {
        return create(chatModel, chatModel, registry);
    }

    /**
     * Creates a DRLAuthoringAgent with simple validation and execution tools.
     * This is the backward-compatible method that creates an agent with an empty registry.
     * 
     * @param chatModel The chat model to use for both planning and generation
     * @return A configured DRLAuthoringAgent with validation and execution tools
     */
    public static UntypedAgent create(ChatModel chatModel) {
        return create(chatModel, new InMemoryFactTypeRegistry());
    }

    /**
     * Creates a DRLAuthoringAgent with an empty registry.
     * 
     * @param chatModel The chat model to use for the agent
     * @return A configured agent with an empty fact type registry
     */
    public static UntypedAgent createWithEmptyRegistry(ChatModel chatModel) {
        return create(chatModel, new InMemoryFactTypeRegistry());
    }

    /**
     * Creates a loop-based DRL authoring agent that iteratively refines DRL code.
     * This provides guaranteed working DRL by continuously validating and executing
     * until both validation and execution succeed.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @param registry The fact type registry to use (can be pre-loaded with existing types)
     * @param maxIterations Maximum number of refinement iterations (default: 3)
     * @return A loop-based DRL authoring agent that guarantees working DRL
     */
    public static UntypedAgent createLoopBasedAgent(ChatModel chatModel, FactTypeRegistry registry, int maxIterations) {
        return DRLAuthoringLoop.create(chatModel, registry, maxIterations);
    }

    /**
     * Creates a loop-based DRL authoring agent with default settings.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @param registry The fact type registry to use
     * @return A loop-based DRL authoring agent with 3 max iterations
     */
    public static UntypedAgent createLoopBasedAgent(ChatModel chatModel, FactTypeRegistry registry) {
        return DRLAuthoringLoop.create(chatModel, registry);
    }

    /**
     * Creates a loop-based DRL authoring agent with empty registry.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @return A loop-based DRL authoring agent with empty registry
     */
    public static UntypedAgent createLoopBasedAgent(ChatModel chatModel) {
        return DRLAuthoringLoop.create(chatModel);
    }
}