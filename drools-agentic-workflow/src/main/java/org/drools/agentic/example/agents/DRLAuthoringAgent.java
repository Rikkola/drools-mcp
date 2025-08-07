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
 * Drools DRL authoring agent interface for generating DRL code from natural language.
 * Enhanced with fact type registry capabilities for reusing and managing fact type declarations.
 */
public interface DRLAuthoringAgent {

    @SystemMessage("""
        You are an advanced Drools DRL language authoring agent with fact type registry capabilities.
        You can generate complete DRL code while leveraging existing fact type definitions.

        ENHANCED CAPABILITIES:
        1. Query existing fact type definitions using registry tools
        2. Reuse existing fact types when they match requirements
        3. Extend existing fact types by adding new fields when needed
        4. Create new fact types only when no suitable existing type exists
        5. Always update the registry with any new or modified fact types
        6. Ensure consistency across fact type definitions

        CRITICAL WORKFLOW:
        1. FIRST: Check the registry for existing fact types using getExistingFactTypes()
        2. ANALYZE: Determine if existing types can be reused or need modification
        3. REUSE: Use existing compatible fact types directly in your rules
        4. EXTEND: Add missing fields to existing types using addFieldToFactType()
        5. CREATE: Only create new fact types when necessary using updateFactType()
        6. GENERATE: Use generateAllDRLDeclarations() to get the complete declare blocks

        FACT TYPE MANAGEMENT RULES:
        - Always prefer reusing existing fact types over creating new ones
        - When extending a fact type, preserve existing field names and types
        - Use consistent naming conventions across related fact types
        - Ensure field types are compatible (String, int, boolean, double, java.util.Date, etc.)
        - Set appropriate default values for new fields

        DRL GENERATION PROCESS:
        1. Query registry for existing fact types
        2. Determine which types to use/modify/create based on requirements
        3. Update registry with any changes
        4. Generate DRL declarations from registry
        5. Write business rules that use the declared types
        6. Validate and test the complete DRL

        EXAMPLE WORKFLOW:
        Input: "Create rules for customer orders with discounts"
        1. Check registry: getExistingFactTypes()
        2. Found existing "Order" type with id, amount fields
        3. Add discount fields: addFieldToFactType("Order", "discountRate", "double", "0.0")
        4. Create Customer if needed: updateFactType("Customer", ...)
        5. Generate declarations: generateAllDRLDeclarations()
        6. Write business rules using Order and Customer types
        7. Test with execution tool

        Always include proper package declarations and ensure the '_type' field is present in test JSON data.
        Format: [{"_type":"Order", "id":"ORD001", "amount":150.0, "discountRate":0.1}]
        """)
    @UserMessage("{{request}}")
    @Agent("An advanced Drools DRL authoring agent with fact type registry")
    String handleRequest(@V("request") String request);

    /**
     * Creates a DRLAuthoringAgent with registry, validation and execution tools.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @param registry The fact type registry to use (can be pre-loaded with existing types)
     * @return A configured agent with fact type registry capabilities
     */
    static DRLAuthoringAgent create(ChatModel chatModel, FactTypeRegistry registry) {
        SimpleDRLValidationToolService validationService = new SimpleDRLValidationToolService();
        SimpleDRLExecutionToolService executionService = new SimpleDRLExecutionToolService();
        FactTypeRegistryToolService registryService = new FactTypeRegistryToolService(registry);

        return AgenticServices.agentBuilder(DRLAuthoringAgent.class)
                .chatModel(chatModel)
                .tools(validationService, executionService, registryService)
                .build();
    }

    /**
     * Creates a DRLAuthoringAgent with simple validation and execution tools.
     * This is the backward-compatible method that creates an agent with an empty registry.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @return A configured DRLAuthoringAgent with validation and execution tools
     */
    static DRLAuthoringAgent create(ChatModel chatModel) {
        return create(chatModel, new InMemoryFactTypeRegistry());
    }

    /**
     * Creates a DRLAuthoringAgent with an empty registry.
     * 
     * @param chatModel The chat model to use for the agent
     * @return A configured agent with an empty fact type registry
     */
    static DRLAuthoringAgent createWithEmptyRegistry(ChatModel chatModel) {
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
    static UntypedAgent createLoopBasedAgent(ChatModel chatModel, FactTypeRegistry registry, int maxIterations) {
        return DRLAuthoringLoop.create(chatModel, registry, maxIterations);
    }

    /**
     * Creates a loop-based DRL authoring agent with default settings.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @param registry The fact type registry to use
     * @return A loop-based DRL authoring agent with 3 max iterations
     */
    static UntypedAgent createLoopBasedAgent(ChatModel chatModel, FactTypeRegistry registry) {
        return DRLAuthoringLoop.create(chatModel, registry);
    }

    /**
     * Creates a loop-based DRL authoring agent with empty registry.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @return A loop-based DRL authoring agent with empty registry
     */
    static UntypedAgent createLoopBasedAgent(ChatModel chatModel) {
        return DRLAuthoringLoop.create(chatModel);
    }
}