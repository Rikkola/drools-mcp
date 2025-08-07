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
        You are a DRL code generator. Your job is to:

        1. FIRST: Check the registry for existing fact types using getExistingFactTypes()
        2. GENERATE: Create complete DRL code with package, declare blocks, and rules
        3. STORE: Save the generated DRL code to cognisphere state as "current_drl"
        4. IMPROVE: If validation/execution feedback exists, refine the DRL accordingly

        Read previous feedback from cognisphere:
        - validation_feedback: syntax/structure issues to fix
        - execution_feedback: runtime/logic issues to address

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

        Always include proper package declarations and ensure the DRL is complete and syntactically correct.

        CRITICAL: Always save your DRL output to cognisphere.writeState("current_drl", drlCode)
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