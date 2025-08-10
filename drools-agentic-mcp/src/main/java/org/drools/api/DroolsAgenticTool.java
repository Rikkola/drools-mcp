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
import org.drools.agentic.example.agents.DroolsWorkflowOrchestratorAgent;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.services.execution.KnowledgeRunnerService;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.data.message.UserMessage;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DroolsAgenticTool {

    // No need for instance variable since DroolsWorkflowOrchestratorAgent provides static methods
    private final KnowledgeRunnerService knowledgeRunnerService;

    public DroolsAgenticTool() {
        this.knowledgeRunnerService = new KnowledgeRunnerService();
    }

    @Tool(description = "Enhance and expand the knowledge base with new business logic based on detailed specifications. This tool analyzes domain requirements and implements sophisticated decision-making capabilities.")
    public String improveKnowledgeBase(@ToolArg(description = "Comprehensive specification including: 1) Domain model (entities, attributes, relationships), 2) Business constraints and validation rules, 3) Decision logic requirements, 4) Example scenarios with expected outcomes, 5) Integration requirements. Provide as much detail as possible - this serves as the design document for implementation.") String specification) {
        try {
            // Create agent workflow with default chat models
            UntypedAgent agentWorkflow = DroolsWorkflowOrchestratorAgent.create(
                ChatModels.OLLAMA_GRANITE_PLANNING_MODEL, 
                ChatModels.OLLAMA_GRANITE_CODE_MODEL
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
    public String executeRules(@ToolArg(description = "JSON facts to insert and execute rules against. Each fact can optionally include a '_type' field to specify the object type for dynamic object creation. Example: [{\"_type\":\"Person\", \"name\":\"John\", \"age\":25}]") String jsonFacts,
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
    public String executeBatch(@ToolArg(description = "JSON array of fact batches to process. Each fact can optionally include a '_type' field for dynamic object creation. Example: [[[{\"_type\":\"Person\", \"name\":\"John\"}], [{\"name\":\"Jane\", \"age\":30}]]]") String jsonFactBatches,
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

    @Prompt(description = "Generate properly formatted JSON facts for rule execution with examples and validation")
    public PromptMessage jsonFactsGuide(
            @PromptArg(description = "The domain entities/types to create facts for") String entityTypes) {
        
        // Use default if not provided
        if (entityTypes == null || entityTypes.trim().isEmpty()) {
            entityTypes = "Person, Order";
        }
        
        String guide = """
            # JSON Facts Generation Guide for %s Entities
            
            ## JSON Format Requirements
            Facts must be provided as a JSON array of objects where each object represents an entity instance.
            
            ## Basic Structure with _type Field (Recommended)
            ```json
            [
              {
                "_type": "ObjectType",
                "attribute1": "value1",
                "attribute2": "value2"
              }
            ]
            ```
            
            ⚠️ **Important**: Include the `_type` field to enable dynamic object creation and proper rule matching.
            Without `_type`, facts are inserted as generic Map objects which may not match typed rule patterns.
            
            ## Alternative Structure (Map Objects)
            ```json
            [
              {
                "attribute1": "value1",
                "attribute2": "value2"
              }
            ]
            ```
            
            ## Data Type Guidelines
            - **Strings**: Use double quotes: "John Doe"
            - **Numbers**: No quotes: 25, 99.99
            - **Booleans**: No quotes: true, false
            - **Dates**: ISO format strings: "2024-01-15"
            - **Nested Objects**: Full object structure
            
            ## Example for Common Entity Types
            
            ### Person Entity:
            ```json
            [
              {
                "_type": "Person",
                "name": "Alice Johnson",
                "age": 30,
                "email": "alice@example.com",
                "active": true,
                "registrationDate": "2023-06-15"
              },
              {
                "_type": "Person",
                "name": "Bob Smith", 
                "age": 17,
                "email": "bob@example.com",
                "active": true,
                "registrationDate": "2024-01-10"
              }
            ]
            ```
            
            ### Order Entity:
            ```json
            [
              {
                "_type": "Order",
                "orderId": "ORD-001",
                "amount": 150.75,
                "currency": "USD",
                "orderDate": "2024-08-06",
                "customerId": "CUST-123",
                "status": "PROCESSING",
                "items": 3
              }
            ]
            ```
            
            ### Complex Example with Relationships:
            ```json
            [
              {
                "_type": "Customer",
                "name": "Premium Customer",
                "totalSpent": 1500.00,
                "membershipLevel": "GOLD",
                "orders": [
                  {
                    "amount": 250.00,
                    "date": "2024-07-15"
                  }
                ]
              }
            ]
            ```
            
            ## Validation Checklist
            ✓ Valid JSON syntax (use JSON validator)
            ✓ Include `_type` field for proper object creation
            ✓ Consistent attribute names across objects
            ✓ Appropriate data types for each field
            ✓ Required fields are present
            ✓ Realistic test data values
            
            ## Common Mistakes to Avoid
            ❌ Missing `_type` field (leads to Map objects instead of typed objects)
            ❌ Single quotes instead of double quotes
            ❌ Trailing commas in JSON
            ❌ Missing quotes on string values
            ❌ Inconsistent attribute naming
            ❌ Invalid date formats
            
            Use the executeRules tool with your generated JSON facts to test your business logic!
            """.formatted(entityTypes);
        
        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Generate batch execution scenarios with multiple fact sets for comprehensive testing")
    public PromptMessage batchExecutionGuide(
            @PromptArg(description = "The testing scenario or use case") String scenario) {
        
        // Use default if not provided  
        if (scenario == null || scenario.trim().isEmpty()) {
            scenario = "customer classification";
        }
        
        String guide = """
            # Batch Execution Guide for %s Testing
            
            ## Purpose of Batch Execution
            Test multiple scenarios simultaneously to validate business logic comprehensively.
            Each batch represents a different test case or data scenario.
            
            ## Batch Structure
            ```json
            [
              [/* Batch 1: Test Case 1 facts */],
              [/* Batch 2: Test Case 2 facts */],
              [/* Batch 3: Test Case 3 facts */]
            ]
            ```
            
            ## Testing Strategy
            
            ### 1. Edge Case Testing
            ```json
            [
              [/* Normal cases */],
              [/* Boundary values */], 
              [/* Edge cases */],
              [/* Invalid data */]
            ]
            ```
            
            ### 2. Customer Classification Example
            ```json
            [
              [
                {
                  "name": "Regular Customer",
                  "totalSpent": 250.00,
                  "orderCount": 5,
                  "memberSince": "2024-01-15"
                }
              ],
              [
                {
                  "name": "VIP Customer", 
                  "totalSpent": 1500.00,
                  "orderCount": 25,
                  "memberSince": "2022-03-10"
                }
              ],
              [
                {
                  "name": "New Customer",
                  "totalSpent": 0.00,
                  "orderCount": 0, 
                  "memberSince": "2024-08-06"
                }
              ]
            ]
            ```
            
            ### 3. Order Processing Scenarios
            ```json
            [
              [
                {
                  "orderId": "ORD-001",
                  "amount": 50.00,
                  "customerType": "REGULAR",
                  "shippingDistance": 25
                }
              ],
              [
                {
                  "orderId": "ORD-002", 
                  "amount": 150.00,
                  "customerType": "PREMIUM",
                  "shippingDistance": 100
                }
              ],
              [
                {
                  "orderId": "ORD-003",
                  "amount": 300.00, 
                  "customerType": "VIP",
                  "shippingDistance": 500
                }
              ]
            ]
            ```
            
            ## Advanced Testing Patterns
            
            ### Progressive Complexity
            1. **Simple Cases**: Single entity, basic attributes
            2. **Relationship Cases**: Multiple related entities  
            3. **Complex Cases**: Deep object hierarchies
            4. **Stress Cases**: Large data sets
            
            ### Comprehensive Coverage
            - ✓ Happy path scenarios
            - ✓ Business rule boundaries (>=, <=, ==)
            - ✓ Exception conditions
            - ✓ Data validation scenarios
            - ✓ Integration edge cases
            
            ## Performance Considerations
            - Start with small batches (3-5 scenarios)
            - Monitor execution times
            - Use maxActivations parameter to prevent infinite loops
            - Consider memory usage with large fact sets
            
            ## Result Analysis
            After batch execution:
            1. Review rule activation counts per batch
            2. Verify expected vs actual outcomes
            3. Identify performance bottlenecks
            4. Document successful test patterns
            
            Use the executeBatch tool with your scenarios to validate business logic comprehensively!
            """.formatted(scenario);
        
        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Guide for interpreting knowledge base status and troubleshooting common issues")
    public PromptMessage knowledgeBaseStatusGuide() {
        
        String guide = """
            # Knowledge Base Status Interpretation Guide
            
            ## Understanding Status Output
            
            ### Key Status Indicators
            
            #### 📋 Knowledge Base Details
            - **Name**: Identifier of the current knowledge base
            - **Release ID**: Version/build identifier of the compiled rules
            - **Session Active**: Whether a rule session is currently running
            - **Session ID**: Unique identifier for the current session
            - **Facts in Memory**: Number of facts currently in working memory
            - **Created**: Timestamp when the knowledge base was built
            - **Source**: Information about the DRL source files used
            
            ## Status Scenarios
            
            ### ✅ Healthy Status
            ```
            📋 Knowledge Base Details:
              • Name: BusinessRules-v1.0
              • Release ID: 1.0.0-SNAPSHOT
              • Session Active: Yes
              • Session ID: session-123456
              • Facts in Memory: 15
              • Created: 2024-08-06T10:30:15
              • Source: person-rules.drl, order-rules.drl
            ```
            **Interpretation**: System ready for rule execution
            
            ### ⚠️ No Knowledge Base
            ```
            ❌ No knowledge base available in shared storage.
            Please use DroolsKnowledgeBaseService to build a knowledge base first.
            ```
            **Resolution**: 
            1. Use improveKnowledgeBase to create business logic
            2. Verify DRL files exist in storage
            3. Check for compilation errors
            
            ### 🔄 Session Issues
            ```
            📋 Session Active: No
            📋 Facts in Memory: 0
            ```
            **Possible Causes**:
            - Session was disposed
            - Knowledge base was rebuilt
            - System restart cleared session
            
            ## Troubleshooting Guide
            
            ### Common Issues & Solutions
            
            #### Issue: "No knowledge base available"
            **Symptoms**: Status shows no knowledge base
            **Solutions**:
            1. Create business logic with improveKnowledgeBase
            2. Check if DRL files exist in ~/.drools-agent-storage
            3. Verify DRL syntax is valid
            
            #### Issue: "Session not active" 
            **Symptoms**: Session Active: No
            **Solutions**:
            1. Knowledge base exists but session disposed
            2. Re-execute rules to reactivate session
            3. Clear facts if session is stale
            
            #### Issue: High facts in memory
            **Symptoms**: Facts in Memory > 1000
            **Solutions**:
            1. Use clearFacts to reset working memory
            2. Review rule logic for fact accumulation
            3. Check for infinite loops in rules
            
            #### Issue: Old creation timestamp
            **Symptoms**: Created timestamp is very old
            **Solutions**:
            1. Knowledge base may be stale
            2. Rebuild with updated business logic
            3. Verify source files are current
            
            ## Monitoring Best Practices
            
            ### Regular Health Checks
            - Check status before rule execution
            - Monitor fact count growth over time
            - Track session lifecycle
            - Verify knowledge base currency
            
            ### Performance Indicators
            - **Low fact count**: Efficient rule execution
            - **Active session**: System ready for work
            - **Recent creation**: Up-to-date business logic
            - **Clear source info**: Traceable rule origins
            
            ### Maintenance Actions
            - **Periodic clearFacts**: Prevent memory bloat
            - **Regular rebuilds**: Keep logic current  
            - **Session monitoring**: Detect performance issues
            - **Status logging**: Track system health
            
            Use getKnowledgeBaseStatus regularly to maintain optimal system health!
            """;
        
        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Guide for knowledge base session management and fact lifecycle best practices")
    public PromptMessage sessionManagementGuide() {
        
        String guide = """
            # Knowledge Base Session Management Guide
            
            ## Understanding Session Lifecycle
            
            ### Session States
            1. **Inactive**: No knowledge base loaded
            2. **Active**: Knowledge base loaded, ready for execution
            3. **Processing**: Rules are executing
            4. **Memory Full**: Many facts accumulated
            5. **Stale**: Long-running session with old data
            
            ## When to Clear Facts
            
            ### ✅ Clear Facts When:
            - **Between test scenarios**: Isolate different test cases
            - **High memory usage**: Facts in Memory > 500-1000
            - **Stale data**: Facts represent old/outdated information
            - **New business context**: Switching to different use case
            - **Performance degradation**: Execution becoming slow
            - **Debug sessions**: Need clean state for troubleshooting
            
            ### ❌ Don't Clear Facts When:
            - **Incremental processing**: Building up state over time
            - **Batch workflows**: Processing related data sets
            - **Stateful rules**: Rules depend on accumulated facts
            - **Active transactions**: Mid-process operations
            - **Performance testing**: Need realistic data volumes
            
            ## Session Management Patterns
            
            ### Pattern 1: Test Isolation
            ```
            1. clearFacts()           // Clean slate
            2. executeRules(testData) // Run test
            3. Analyze results
            4. clearFacts()           // Clean for next test
            ```
            
            ### Pattern 2: Batch Processing  
            ```
            1. clearFacts()                    // Start clean
            2. executeBatch(multipleSets)      // Process batches
            3. Analyze aggregate results
            4. clearFacts()                    // Final cleanup
            ```
            
            ### Pattern 3: Incremental Updates
            ```
            1. executeRules(baseData)     // Establish baseline
            2. executeRules(deltaData)    // Add new facts
            3. Continue processing...
            4. clearFacts() when complete
            ```
            
            ### Pattern 4: Periodic Maintenance
            ```
            1. Monitor fact count growth
            2. clearFacts() when threshold reached
            3. Re-establish baseline facts if needed
            4. Continue processing
            ```
            
            ## Memory Management Best Practices
            
            ### Proactive Management
            - **Set fact thresholds**: Clear when count exceeds limits
            - **Time-based clearing**: Clear after time intervals
            - **Context switching**: Clear between different domains
            - **Performance monitoring**: Clear when execution slows
            
            ### Fact Lifecycle Stages
            1. **Insertion**: Facts added via executeRules
            2. **Processing**: Rules fire and modify facts
            3. **Accumulation**: Facts build up in working memory
            4. **Clearing**: Facts removed via clearFacts
            5. **Reset**: Clean working memory state
            
            ## Production Considerations
            
            ### Monitoring Metrics
            - **Fact count trends**: Growth patterns over time
            - **Execution performance**: Rule firing times
            - **Memory usage**: System resource consumption
            - **Session uptime**: How long sessions remain active
            
            ### Automated Management
            ```javascript
            // Pseudo-code for automated fact management
            if (factCount > maxThreshold) {
                clearFacts();
                reloadBaseFacts();
            }
            
            if (sessionAge > maxAge) {
                clearFacts(); 
                refreshKnowledgeBase();
            }
            ```
            
            ### Error Recovery
            - **Session corruption**: Clear and restart
            - **Memory leaks**: Periodic clearing
            - **Rule conflicts**: Clear and debug
            - **Data inconsistency**: Clear and reload
            
            ## Advanced Scenarios
            
            ### Multi-Tenant Systems
            - Clear facts between different customers/tenants
            - Isolate data between business units
            - Prevent data leakage between contexts
            
            ### Development & Testing
            - Clear between unit tests
            - Fresh state for integration tests  
            - Clean data for performance benchmarks
            - Isolated debugging sessions
            
            ### Long-Running Services
            - Periodic maintenance clearing
            - Fact expiration policies
            - Memory pressure handling
            - Session health monitoring
            
            ## Common Patterns Summary
            
            | Use Case | Clear Frequency | Rationale |
            |----------|----------------|-----------|
            | Unit Testing | After each test | Isolation |
            | Batch Processing | After each batch | Memory management |
            | Interactive Usage | User discretion | Context switching |
            | Production Service | Threshold-based | Performance |
            | Development/Debug | Frequently | Clean state |
            
            Use clearFacts strategically to maintain optimal system performance and data integrity!
            """;
        
        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Generate complete end-to-end workflow examples from specification to execution")
    public PromptMessage workflowGuide(
            @PromptArg(description = "The business process or workflow type") String workflowType) {
        
        // Use default if not provided
        if (workflowType == null || workflowType.trim().isEmpty()) {
            workflowType = "customer onboarding";
        }
        
        String guide = """
            # Complete End-to-End Workflow Guide for %s
            
            ## Overview
            This guide demonstrates the complete workflow from business specification to rule execution and validation.
            
            ## Phase 1: Business Logic Specification
            
            ### Step 1: Get Specification Guidance
            ```json
            {
              "prompt": "businessLogicSpecificationGuide",
              "parameters": {
                "domain": "%s"
              }
            }
            ```
            
            ### Step 2: Create Detailed Specification
            Example specification for %s:
            ```
            Domain Model: 
            - Customer entity with name (String), email (String), age (Integer), country (String)
            - Application entity with applicationId (String), status (String), riskScore (Integer)
            
            Business Rules:
            1. Customers under 18 are automatically rejected
            2. High-risk countries require additional verification (riskScore > 70)
            3. Customers over 65 get priority processing
            4. Email domains must be from approved list
            5. Applications with riskScore > 90 are flagged for manual review
            
            Constraints:
            - Age must be between 0-120
            - Email must contain @ symbol and valid domain
            - Country must be ISO code
            - Risk score between 0-100
            
            Examples:
            - Customer(name="John", email="john@gmail.com", age=25, country="US") → Approved
            - Customer(name="Jane", email="jane@suspicious.com", age=17, country="XX") → Rejected
            - Customer(name="Bob", email="bob@bank.com", age=70, country="CA") → Priority + Approved
            ```
            
            ## Phase 2: Implementation
            
            ### Step 3: Implement Business Logic
            ```json
            {
              "tool": "improveKnowledgeBase",
              "parameters": {
                "specification": "[Your detailed specification from Phase 1]"
              }
            }
            ```
            
            ## Phase 3: Validation & Testing
            
            ### Step 4: Check System Status
            ```json
            {
              "tool": "getKnowledgeBaseStatus"
            }
            ```
            
            ### Step 5: Prepare Test Data
            Use the JSON facts guide:
            ```json
            {
              "prompt": "jsonFactsGuide", 
              "parameters": {
                "entityTypes": "Customer, Application"
              }
            }
            ```
            
            ### Step 6: Single Test Execution
            ```json
            {
              "tool": "executeRules",
              "parameters": {
                "jsonFacts": "[
                  {
                    \"name\": \"Alice Johnson\",
                    \"email\": \"alice@gmail.com\", 
                    \"age\": 30,
                    \"country\": \"US\",
                    \"applicationId\": \"APP-001\",
                    \"status\": \"PENDING\",
                    \"riskScore\": 45
                  }
                ]",
                "maxActivations": 0
              }
            }
            ```
            
            ## Phase 4: Comprehensive Testing
            
            ### Step 7: Batch Testing Setup
            ```json
            {
              "prompt": "batchExecutionGuide",
              "parameters": {
                "scenario": "%s testing"
              }
            }
            ```
            
            ### Step 8: Execute Batch Tests
            ```json
            {
              "tool": "executeBatch",
              "parameters": {
                "jsonFactBatches": "[
                  [
                    {
                      \"name\": \"Young Applicant\",
                      \"email\": \"young@example.com\",
                      \"age\": 16,
                      \"country\": \"US\",
                      \"riskScore\": 20
                    }
                  ],
                  [
                    {
                      \"name\": \"Senior Applicant\", 
                      \"email\": \"senior@example.com\",
                      \"age\": 70,
                      \"country\": \"CA\",
                      \"riskScore\": 30
                    }
                  ],
                  [
                    {
                      \"name\": \"High Risk Applicant\",
                      \"email\": \"risk@suspicious.com\", 
                      \"age\": 35,
                      \"country\": \"XX\",
                      \"riskScore\": 95
                    }
                  ]
                ]",
                "maxActivations": 0
              }
            }
            ```
            
            ## Phase 5: Session Management
            
            ### Step 9: Session Monitoring
            ```json
            {
              "prompt": "sessionManagementGuide"
            }
            ```
            
            ### Step 10: Clean Up Between Tests
            ```json
            {
              "tool": "clearFacts"
            }
            ```
            
            ## Common Workflow Patterns
            
            ### Development Workflow
            1. **Design**: Use businessLogicSpecificationGuide
            2. **Implement**: Use improveKnowledgeBase  
            3. **Test**: Single executeRules calls
            4. **Debug**: Check status, clear facts, retry
            5. **Validate**: Batch testing with executeBatch
            
            ### Production Workflow  
            1. **Deploy**: Load knowledge base
            2. **Monitor**: Regular getKnowledgeBaseStatus
            3. **Execute**: Process facts with executeRules
            4. **Maintain**: Periodic clearFacts
            5. **Update**: Re-deploy with improveKnowledgeBase
            
            ### Testing Workflow
            1. **Setup**: Clear facts, check status
            2. **Prepare**: Generate test data with guides
            3. **Execute**: Single or batch execution
            4. **Validate**: Check results and performance
            5. **Cleanup**: Clear facts between test scenarios
            
            ## Best Practices Summary
            
            ### ✅ Do:
            - Start with comprehensive specifications
            - Use batch testing for validation
            - Monitor session health regularly
            - Clear facts between different contexts
            - Follow systematic workflows
            
            ### ❌ Avoid:
            - Skipping specification phase
            - Testing with unrealistic data
            - Ignoring session management
            - Running without status checks
            - Manual repetitive processes
            
            ## Advanced Integration Patterns
            
            ### CI/CD Integration
            1. Automated specification validation
            2. Batch test execution in pipelines
            3. Performance regression testing  
            4. Automated deployment workflows
            
            ### Monitoring & Observability
            1. Status checks in health endpoints
            2. Performance metrics collection
            3. Rule execution audit trails
            4. Session lifecycle tracking
            
            Follow this workflow pattern to ensure reliable, maintainable business logic implementation!
            """.formatted(workflowType, workflowType, workflowType, workflowType);
        
        return PromptMessage.withUserRole(new TextContent(guide));
    }
}
