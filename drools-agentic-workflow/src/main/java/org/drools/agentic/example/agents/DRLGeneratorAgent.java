package org.drools.agentic.example.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.registry.FactTypeRegistry;

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
        2. DECLARE: Generate fact type declarations for all object types used in rules
        3. REFINE: Improve DRL based on validation and execution feedback
        4. OPTIMIZE: Ensure code is efficient and follows Drools best practices

        WORKFLOW INTEGRATION:
        - Read planning context from previous workflow steps
        - Return the generated DRL
        - Read and incorporate feedback from validation/execution phases

        CODE GENERATION STANDARDS:
        - Include proper package declarations
        - Generate complete declare blocks for all fact types used in rules
        - Create fact type declarations with appropriate fields and types
        - Write clear, maintainable business rules
        - Use appropriate Java types (String, int, boolean, double, Date, etc.)
        - Ensure syntactic correctness and Drools compliance
        
        FACT TYPE DECLARATION REQUIREMENTS:
        - Analyze all object types referenced in rules
        - Generate declare blocks for custom fact types
        - Include all necessary fields with proper types
        - Use meaningful field names that reflect business domain
        
        CRITICAL DROOLS CONSTRUCTOR RULES:
        - Drools 'declare' blocks generate classes with ONLY no-arg constructors
        - NEVER use parameterized constructors like new Person("John", 30, true)
        - ALWAYS use no-arg constructor + setter pattern: new Person(); person.setName("John"); person.setAge(30);
        - OR use 'insert(new Person())' for empty objects and separate rules to populate fields
        
        CORRECT DRL PATTERNS:
        ✅ GOOD: 
        rule "Init Person"
        when
        then
            Person person = new Person();
            person.setName("John");  
            person.setAge(30);
            person.setAdult(false);
            insert(person);
        end
        
        ❌ BAD: 
        insert(new Person("John", 30, true));  // Constructor doesn't exist!

        """)
    @UserMessage("Generate DRL for: {{request}}")
    @Agent(outputName="current_drl", value="DRL code generator for loop workflow")
    String generateDRL(@MemoryId String memoryId, @V("request") String document);

    /**
     * Creates a DRLGeneratorAgent with registry tools.
     * 
     * @param chatModel The chat model to use for the agent (must support tools)
     * @param registry The fact type registry to use
     * @return A configured DRL generator agent
     */
    static DRLGeneratorAgent create(ChatModel chatModel, FactTypeRegistry registry) {
        return AgenticServices.agentBuilder(DRLGeneratorAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}
