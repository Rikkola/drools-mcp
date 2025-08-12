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
 * 1. DOCUMENT ANALYSIS + LOOP WORKFLOW: Uses document planning for business knowledge extraction and code gen model for implementation
 * 2. LOOP-ONLY WORKFLOW: Direct loop-based DRL generation with iterative validation and execution
 * 
 * Document Analysis Features:
 * - Analyzes any text input to extract business knowledge
 * - Identifies domain models, entities, and their attributes
 * - Extracts business rules, decisions, and logic flows
 * - Creates technology-agnostic documentation for rule base creation
 * - Focuses on business terminology rather than technical implementation
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
public interface DRLAuthoringAgent {

    /**
     * Document planning agent interface for analyzing text and extracting domain models, rules, and decisions.
     * This agent creates domain-agnostic documentation that can be used for rule base creation.
     */
    public interface DocumentPlanningAgent {
        @SystemMessage("""
            You are a Document Planning Agent responsible for analyzing any text input and extracting business knowledge 
            that can be used to create rule-based systems. Your analysis should be technology-agnostic and focus on 
            business logic rather than implementation details.
            
            YOUR RESPONSIBILITIES:
            1. ANALYZE the provided text to identify business concepts, entities, and relationships
            2. EXTRACT potential business rules, decisions, and logic flows
            3. IDENTIFY domain models, data structures, and entity attributes
            4. CREATE a structured document that captures the business knowledge
            
            ANALYSIS FRAMEWORK:
            
            ## Domain Models & Entities
            - Identify key business entities (nouns: Customer, Order, Product, etc.)
            - Extract entity attributes and their types
            - Map relationships between entities
            - Note any hierarchies or categorizations
            
            ## Business Rules & Logic
            - Extract conditional logic (if-then patterns)
            - Identify validation rules and constraints
            - Find calculation formulas and algorithms
            - Note approval workflows and decision trees
            
            ## Decision Points
            - Identify where decisions are made in the business process
            - Extract decision criteria and conditions
            - Map decision outcomes and actions
            - Note any escalation or exception handling
            
            ## Business Processes
            - Map sequential workflows and process steps
            - Identify trigger events and conditions
            - Extract business constraints and policies
            - Note any temporal or scheduling requirements
            
            OUTPUT FORMAT:
            Provide a structured analysis in markdown format with clear sections for:
            - Executive Summary
            - Domain Models (entities and attributes)
            - Business Rules (conditional logic)
            - Decision Framework (decision points and criteria)
            - Process Flows (workflows and sequences)
            - Implementation Considerations (for rule engine design)
            
            Focus on capturing the WHAT and WHY of the business logic, not the HOW of implementation.
            Be comprehensive but concise. Use business terminology, not technical jargon.
            """)
        @UserMessage("Analyze the following text and extract business knowledge for rule base creation:\n\n{{textInput}}")
        @Agent( outputName="document", value="Business Knowledge Extraction :workflowSpecialist")
        String analyzeDomainFromText(@V("textInput") String textInput);
    }

    @UserMessage("I will take in {{textInput}} and create a knowledge base out of it.")
    @Agent(outputName="current_drl", value="Authoring agent.")
    String author(@V("textInput") String textInput);

    // ========== PUBLIC API METHODS ==========

    /**
     * Creates a DRLAuthoringAgent with document planning and loop-based generation workflow.
     * Uses sequenceBuilder to coordinate between document analysis and generation phases.
     * 
     * @param analysisModel The chat model to use for document analysis (should be good at reasoning and text analysis)
     * @param codeGenModel The chat model to use for code generation (should be good at tools)
     * @param registry The fact type registry to use (can be pre-loaded with existing types)
     * @return A configured orchestration agent with document analysis and generation workflow
     */
    public static DRLAuthoringAgent create(ChatModel analysisModel, ChatModel codeGenModel, FactTypeRegistry registry) {
        // Create document planning agent for business knowledge extraction
        DocumentPlanningAgent documentAgent = AgenticServices.agentBuilder(DocumentPlanningAgent.class)
                .chatModel(analysisModel)
                .build();
        
        // Create loop-based generation workflow for actual DRL creation
        var loopGenerationWorkflow = createLoopWorkflow(codeGenModel, registry, 3);
        
        // Sequence: Document Analysis → Loop-based Generation
        return AgenticServices.sequenceBuilder(DRLAuthoringAgent.class)
                .subAgents(documentAgent, loopGenerationWorkflow)
                .outputName("drl_output")
                .build();
    }

    interface LoopAgent {

      @UserMessage("I will take in {{document}} and create DRL out of it.")
      @Agent(outputName="current_drl", value="Authoring loop.")
      String author(@V("document") String textInput);
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
    public static LoopAgent createLoopWorkflow(ChatModel chatModel, FactTypeRegistry registry, int maxIterations) {
        // Create individual specialized agents - using different models based on capabilities
        DRLGeneratorAgent generatorAgent = DRLGeneratorAgent.create(chatModel, registry);
        DRLExecutorAgent executorAgent = DRLExecutorAgent.create(ChatModels.getToolCallingModel());

        return AgenticServices.loopBuilder(LoopAgent.class)
                .subAgents(generatorAgent, new DRLValidatorAgent(), executorAgent)
                .maxIterations(maxIterations)
                .exitCondition(cognisphere -> {
                    // Continue loop until both validation and execution succeed
                    String validationFeedback = cognisphere.readState("validation_feedback", "");
                    String executionFeedback = cognisphere.readState("execution_feedback", "");
                    boolean isValid = "Code looks good".equals(validationFeedback);
                    boolean executionSuccessful = executionFeedback.isEmpty();
                    return isValid && executionSuccessful;
                })
                .build();
    }

    // ========== CONVENIENCE METHODS ==========

    /**
     * Creates a standalone DocumentPlanningAgent for analyzing text and extracting business knowledge.
     * This agent is domain-agnostic and focuses on extracting rules, decisions, and domain models.
     * 
     * @param analysisModel The chat model to use for document analysis (should be good at reasoning and text analysis)
     * @return A configured document planning agent for business knowledge extraction
     */
    public static DocumentPlanningAgent createDocumentPlanningAgent(ChatModel analysisModel) {
        return AgenticServices.agentBuilder(DocumentPlanningAgent.class)
                .chatModel(analysisModel)
                .build();
    }

    /**
     * Creates a loop-based DRL authoring workflow with default settings.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @param registry The fact type registry for managing fact type definitions
     * @return A configured loop-based DRL authoring agent with 3 max iterations
     */
    public static LoopAgent createLoopWorkflow(ChatModel chatModel, FactTypeRegistry registry) {
        return createLoopWorkflow(chatModel, registry, 3);
    }

    /**
     * Creates a loop-based DRL authoring workflow with empty registry.
     * 
     * @param chatModel The chat model to use for all agents (must support tools)
     * @return A configured loop-based DRL authoring agent with empty registry
     */
    public static LoopAgent createLoopWorkflow(ChatModel chatModel) {
        return createLoopWorkflow(chatModel, new InMemoryFactTypeRegistry());
    }

}
