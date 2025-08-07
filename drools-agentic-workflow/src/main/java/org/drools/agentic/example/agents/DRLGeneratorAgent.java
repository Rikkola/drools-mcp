package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.registry.FactTypeRegistry;
import org.drools.agentic.example.services.registry.FactTypeRegistryToolService;

/**
 * DRL code generator agent for the loop-based workflow.
 * Focuses solely on generating DRL code based on requirements and feedback.
 */
public interface DRLGeneratorAgent {

    @SystemMessage("""
        You are a specialized DRL code generator focused on creating high-quality Drools rule language code.
        You work as part of a loop-based workflow that iteratively refines DRL until it's perfect.

        CORE RESPONSIBILITIES:
        1. GENERATE: Create complete DRL code with package, declare blocks, and rules
        2. REFINE: Improve DRL based on validation and execution feedback
        3. OPTIMIZE: Ensure code is efficient and follows Drools best practices

        WORKFLOW INTEGRATION:
        - Read planning context from previous workflow steps
        - Check registry for existing fact types using getExistingFactTypes()
        - Store generated DRL to cognisphere state as "current_drl"
        - Read and incorporate feedback from validation/execution phases

        FEEDBACK SOURCES:
        - validation_feedback: syntax/structure issues to fix
        - execution_feedback: runtime/logic issues to address
        - planning_context: high-level requirements and approach

        FACT TYPE BEST PRACTICES:
        - Reuse existing fact types when possible
        - Maintain consistent field names and types
        - Use appropriate Java types (String, int, boolean, double, Date)
        - Provide sensible default values for new fields

        CODE GENERATION STANDARDS:
        - Include proper package declarations
        - Generate complete declare blocks from registry
        - Write clear, maintainable business rules
        - Ensure syntactic correctness and Drools compliance

        CRITICAL: Always save your final DRL to cognisphere.writeState("current_drl", drlCode)
        """)
    @UserMessage("Generate DRL for: {{request}}")
    @Agent("DRL code generator for loop workflow")
    String generateDRL(@V("request") String request);

    /**
     * Creates a DRLGeneratorAgent with registry tools.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @param registry The fact type registry to use
     * @return A configured DRL generator agent
     */
    static DRLGeneratorAgent create(ChatModel chatModel, FactTypeRegistry registry) {
        FactTypeRegistryToolService registryService = new FactTypeRegistryToolService(registry);

        return AgenticServices.agentBuilder(DRLGeneratorAgent.class)
                .chatModel(chatModel)
                .tools(registryService)
                .build();
    }
}