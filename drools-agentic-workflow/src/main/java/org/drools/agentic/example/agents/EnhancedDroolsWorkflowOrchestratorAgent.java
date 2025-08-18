package org.drools.agentic.example.agents;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.drools.agentic.example.config.ChatModels;
import org.drools.agentic.example.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Enhanced agent orchestrator with comprehensive state management, error handling,
 * retry logic, progress tracking, and detailed reporting.
 * 
 * This implementation provides production-ready workflow orchestration with:
 * - Real-time progress tracking
 * - Comprehensive error reporting and recovery
 * - Performance metrics and analytics  
 * - Configurable retry logic
 * - Structured logging and interaction tracking
 */
public interface EnhancedDroolsWorkflowOrchestratorAgent {

    @UserMessage("""
      I will take in {{textInput}} and create a knowledge base out of it with enhanced monitoring and error handling.
      Steps:
      1. Create DRL out of the {{textInput}} with comprehensive validation
      2. Save the DRL to file with FileStorageAgent including backup handling
      3. Compile a knowledge base from stored files with error recovery
      
      Each step includes progress tracking, error handling, and performance monitoring.
    """)
    @Agent("Enhanced workflow agent with comprehensive monitoring.")
    String author(@V("textInput") String textInput);

    /**
     * Creates an enhanced sequential agent workflow with comprehensive monitoring.
     * 
     * @param planningModel ChatModel for planning and coordination tasks
     * @param codeGenModel ChatModel for code generation tasks
     * @return Configured enhanced workflow agent
     */
    static EnhancedDroolsWorkflowOrchestratorAgent create(ChatModel planningModel, ChatModel codeGenModel) {
        return create(planningModel, codeGenModel, WorkflowConfiguration.development());
    }

    /**
     * Creates an enhanced workflow with custom configuration.
     * 
     * @param planningModel ChatModel for planning and coordination
     * @param codeGenModel ChatModel for code generation
     * @param config Workflow configuration settings
     * @return Configured enhanced workflow agent
     */
    static EnhancedDroolsWorkflowOrchestratorAgent create(ChatModel planningModel, ChatModel codeGenModel, 
                                                         WorkflowConfiguration config) {
        Logger logger = LoggerFactory.getLogger(EnhancedDroolsWorkflowOrchestratorAgent.class);
        logger.info("🚀 Creating Enhanced Drools Workflow Orchestrator with config: {}", config);
        
        // Initialize workflow infrastructure
        var workflowState = new WorkflowState();
        var metrics = new WorkflowMetrics();
        var reporter = new WorkflowReporter();
        var progressTracker = new ProgressTracker();
        var errorReporter = new EnhancedErrorReporter();
        var interactionLogger = new AgentInteractionLogger();
        
        // Configure progress bar
        progressTracker.setEnableProgressBar(config.isEnableProgressBar());
        
        // Create enhanced agents with monitoring
        var droolsAuthoringAgent = createEnhancedAgent(
            () -> DRLAuthoringAgent.create(planningModel, codeGenModel),
            "DRLAuthoringAgent", config, metrics, errorReporter, interactionLogger
        );
        
        var fileStorageAgent = createEnhancedAgent(
            () -> FileStorageAgent.create(ChatModels.getToolCallingModel()),
            "FileStorageAgent", config, metrics, errorReporter, interactionLogger
        );
        
        var knowledgeBaseAgent = createEnhancedAgent(
            () -> DroolsKnowledgeBaseAgent.create(ChatModels.getToolCallingModel()),
            "DroolsKnowledgeBaseAgent", config, metrics, errorReporter, interactionLogger
        );

        // Create enhanced orchestrator with monitoring wrapper
        EnhancedDroolsWorkflowOrchestratorAgent baseWorkflow = AgenticServices
                .sequenceBuilder(EnhancedDroolsWorkflowOrchestratorAgent.class)
                .subAgents(droolsAuthoringAgent, fileStorageAgent, knowledgeBaseAgent)
                .outputName("result")
                .build();

        // Return wrapped orchestrator with comprehensive monitoring
        return new EnhancedWorkflowWrapper(baseWorkflow, workflowState, metrics, reporter, 
                                          progressTracker, errorReporter, interactionLogger, config);
    }
    
    /**
     * Creates an enhanced agent wrapper with monitoring and error handling.
     */
    private static <T> T createEnhancedAgent(java.util.function.Supplier<T> agentFactory, String agentName,
                                           WorkflowConfiguration config, WorkflowMetrics metrics,
                                           EnhancedErrorReporter errorReporter, 
                                           AgentInteractionLogger interactionLogger) {
        try {
            Logger logger = LoggerFactory.getLogger(EnhancedDroolsWorkflowOrchestratorAgent.class);
            logger.debug("🔧 Creating enhanced agent: {}", agentName);
            interactionLogger.logAgentStart(agentName, "Initialization");
            
            long startTime = System.currentTimeMillis();
            T agent = agentFactory.get();
            long duration = System.currentTimeMillis() - startTime;
            
            interactionLogger.logAgentComplete(agentName, "Initialization", agent, duration);
            metrics.recordToolUsage(agentName + ".create");
            
            return agent;
            
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(EnhancedDroolsWorkflowOrchestratorAgent.class);
            logger.error("❌ Failed to create agent: {}", agentName, e);
            errorReporter.reportAgentError(agentName, "Initialization", e, null);
            metrics.recordError("AgentCreation", e.getClass().getSimpleName());
            throw new RuntimeException("Failed to create " + agentName, e);
        }
    }

    /**
     * Wrapper class that provides enhanced workflow functionality.
     */
    class EnhancedWorkflowWrapper implements EnhancedDroolsWorkflowOrchestratorAgent {
        private final EnhancedDroolsWorkflowOrchestratorAgent delegate;
        private final WorkflowState workflowState;
        private final WorkflowMetrics metrics;
        private final WorkflowReporter reporter;
        private final ProgressTracker progressTracker;
        private final EnhancedErrorReporter errorReporter;
        private final AgentInteractionLogger interactionLogger;
        private final WorkflowConfiguration config;

        public EnhancedWorkflowWrapper(EnhancedDroolsWorkflowOrchestratorAgent delegate,
                                     WorkflowState workflowState, WorkflowMetrics metrics,
                                     WorkflowReporter reporter, ProgressTracker progressTracker,
                                     EnhancedErrorReporter errorReporter, 
                                     AgentInteractionLogger interactionLogger,
                                     WorkflowConfiguration config) {
            this.delegate = delegate;
            this.workflowState = workflowState;
            this.metrics = metrics;
            this.reporter = reporter;
            this.progressTracker = progressTracker;
            this.errorReporter = errorReporter;
            this.interactionLogger = interactionLogger;
            this.config = config;
        }

        @Override
        public String author(String textInput) {
            // Initialize workflow tracking
            metrics.recordWorkflowStart();
            reporter.reportWorkflowStart("Enhanced Drools Workflow", textInput);
            
            // Setup progress tracking
            progressTracker.updateProgress("Overall", 0, 100, "Initializing workflow");
            
            try {
                // Phase 1: DRL Authoring
                workflowState.startPhase("DRL_AUTHORING");
                progressTracker.updateProgress("Overall", 10, 100, "Starting DRL authoring");
                
                String result = executeWithRetryAndMonitoring(
                    () -> delegate.author(textInput),
                    "Workflow Execution",
                    textInput
                );
                
                // Complete workflow successfully
                progressTracker.updateProgress("Overall", 100, 100, "Workflow completed");
                workflowState.completePhase("WORKFLOW", result);
                metrics.recordWorkflowSuccess();
                
                // Generate final report
                reporter.reportWorkflowComplete(workflowState);
                
                if (config.isEnableMetrics()) {
                    metrics.generateReport();
                }
                
                return result;
                
            } catch (Exception e) {
                Logger logger = LoggerFactory.getLogger(EnhancedWorkflowWrapper.class);
                logger.error("❌ Workflow failed: {}", e.getMessage(), e);
                
                // Record failure
                metrics.recordWorkflowFailure();
                workflowState.addError("WORKFLOW", e.getMessage());
                
                // Comprehensive error reporting
                errorReporter.reportAgentError("WorkflowOrchestrator", "EXECUTION", e, textInput);
                
                // Generate failure report
                reporter.reportWorkflowComplete(workflowState);
                
                if (config.isEnableMetrics()) {
                    metrics.generateReport();
                }
                
                throw new RuntimeException("Enhanced workflow execution failed", e);
            }
        }
        
        private String executeWithRetryAndMonitoring(java.util.function.Supplier<String> operation, 
                                                   String operationName, String context) {
            if (!config.isEnableRetry()) {
                return operation.get();
            }
            
            return RetryUtils.withRetry(operation, config.getRetryConfig(), operationName);
        }
    }
}
