package org.drools.api;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.TextContent;
import org.drools.exception.DefinitionNotFoundException;
import org.drools.exception.DRLExecutionException;
import org.drools.exception.DRLValidationException;
import org.drools.model.JsonResponseBuilder;
import org.drools.agentic.example.workflows.DroolsWorkflowOrchestrator;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.services.execution.KnowledgeRunnerService;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.data.message.UserMessage;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DroolsAgenticTool {

    private final DroolsWorkflowOrchestrator droolsWorkflowOrchestrator;
    private final KnowledgeRunnerService knowledgeRunnerService;

    public DroolsAgenticTool() {
        this.droolsWorkflowOrchestrator = new DroolsWorkflowOrchestrator();
        this.knowledgeRunnerService = new KnowledgeRunnerService();
    }

    @Tool(description = "Enhance and expand the knowledge base with new business logic based on detailed specifications. This tool analyzes domain requirements and implements sophisticated decision-making capabilities.")
    public String improveKnowledgeBase(@ToolArg(description = "Comprehensive specification including: 1) Domain model (entities, attributes, relationships), 2) Business constraints and validation rules, 3) Decision logic requirements, 4) Example scenarios with expected outcomes, 5) Integration requirements. Provide as much detail as possible - this serves as the design document for implementation.") String specification) {
        try {
            // Create agent workflow with default chat models
            UntypedAgent agentWorkflow = droolsWorkflowOrchestrator.createAgentWorkflow(
                ChatModels.OLLAMA_GRANITE_MODEL, 
                ChatModels.OLLAMA_GRANITE3_MOE_MODEL
            );
            
            // Execute the workflow with the specification
            Object result = agentWorkflow.invoke(Map.of(
                "request", "Implement the following business logic specification: " + specification
            ));
            
            return JsonResponseBuilder.create()
                .success()
                .field("result", "Business logic implementation completed")
                .field("details", result)
                .build();
            
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Error implementing business logic: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Execute rules with JSON facts using the shared knowledge base")
    public String executeRules(@ToolArg(description = "JSON facts to insert and execute rules against") String jsonFacts,
                              @ToolArg(description = "Maximum rule activations (0 for unlimited)") Integer maxActivations) {
        try {
            String result = knowledgeRunnerService.executeRules(jsonFacts, maxActivations);
            
            return JsonResponseBuilder.create()
                .success()
                .field("executionResult", result)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Rule execution failed: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Get status and information about the shared knowledge base")
    public String getKnowledgeBaseStatus() {
        try {
            String status = knowledgeRunnerService.getKnowledgeBaseStatus();
            
            return JsonResponseBuilder.create()
                .success()
                .field("knowledgeBaseStatus", status)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Failed to get knowledge base status: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Clear all facts from the shared knowledge base session")
    public String clearFacts() {
        try {
            String result = knowledgeRunnerService.clearFacts();
            
            return JsonResponseBuilder.create()
                .success()
                .field("clearResult", result)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Failed to clear facts: " + e.getMessage())
                .build();
        }
    }

    @Tool(description = "Execute rules multiple times with different fact sets in batch mode")
    public String executeBatch(@ToolArg(description = "JSON array of fact batches to process") String jsonFactBatches,
                              @ToolArg(description = "Maximum rule activations per batch (0 for unlimited)") Integer maxActivations) {
        try {
            String result = knowledgeRunnerService.executeBatch(jsonFactBatches, maxActivations);
            
            return JsonResponseBuilder.create()
                .success()
                .field("batchExecutionResult", result)
                .build();
                
        } catch (Exception e) {
            return JsonResponseBuilder.create()
                .error("Batch execution failed: " + e.getMessage())
                .build();
        }
    }

    @Prompt(description = "Generate a comprehensive business logic specification for the knowledge base implementation system")
    public PromptMessage businessLogicSpecificationGuide(
            @PromptArg(description = "The business domain or use case") String domain) {
        
        // Use default domain if not provided
        if (domain == null || domain.trim().isEmpty()) {
            domain = "business";
        }
        
        String guide = """
            # Business Logic Specification Guide for %s Domain
            
            To effectively use the knowledge base implementation system, provide a comprehensive specification including:
            
            ## 1. Domain Model
            Define your core entities with attributes and types:
            - Entity names and their relationships
            - Attribute names, data types, and constraints
            - Example: Customer(id: Long, name: String, email: String, registrationDate: Date)
            
            ## 2. Business Rules & Decision Logic
            Specify the business rules and decision-making logic:
            - Conditional logic and validation rules
            - Classification and categorization criteria
            - Calculations and derivations
            - Example: "Classify customers as VIP if total orders > $1000 in last 12 months"
            
            ## 3. Constraints & Validations
            Define data validation and business constraints:
            - Required fields and format validations
            - Range checks and business invariants
            - Cross-field validations
            - Example: "Email must contain @ symbol, age must be non-negative"
            
            ## 4. Example Scenarios
            Provide concrete examples with expected outcomes:
            - Sample data inputs
            - Expected classification results
            - Edge cases and boundary conditions
            - Example: "Customer(name='John', totalOrders=1500) → should be classified as VIP"
            
            ## 5. Integration Requirements
            Specify how this fits into your broader system:
            - Input/output formats (JSON, XML, etc.)
            - Performance requirements
            - Error handling expectations
            
            ## Example Complete Specification:
            ```
            Domain Model: Order entity with amount (BigDecimal), date (LocalDate), customer (Customer reference).
            Customer entity with name (String), email (String), totalSpent (BigDecimal).
            
            Business Rules: 
            1. Orders over $100 get free shipping
            2. Customers with totalSpent > $500 are premium customers
            3. Premium customers get 10%% additional discount on orders > $50
            
            Constraints: 
            - Order amount must be positive
            - Email format validation required
            - Customer name cannot be empty
            
            Examples:
            - Customer(name="Alice", email="alice@example.com", totalSpent=600) with Order(amount=75) 
              → Premium customer gets 10%% discount, final amount: $67.50
            - Order(amount=150) → Free shipping applies
            ```
            
            Use the improveKnowledgeBase tool with this detailed specification to implement sophisticated business logic!
            """.formatted(domain);
        
        return PromptMessage.withUserRole(new TextContent(guide));
    }
}
