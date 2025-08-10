# CLAUDE.md - Drools Agentic Examples

This module contains executable examples demonstrating Drools agentic workflow capabilities.

## Module Overview

This module provides runnable examples that showcase the integration of Drools with AI agents using LangChain4j. All examples follow the `ExampleMain` naming convention and provide comprehensive demonstrations of different workflow patterns.

## Structure

```
drools-agentic-examples/
├── pom.xml
├── CLAUDE.md (this file)
└── src/main/java/org/drools/agentic/example/main/
    ├── ModelSelector.java                           # Centralized model selection utility
    ├── DRLAuthoringLoopExampleMain.java            # Loop-based DRL authoring workflow (uses DRLAuthoringAgent.createLoopWorkflow)
    ├── DRLHybridLoopExampleMain.java               # Hybrid loop with non-AI validation
    ├── DroolsWorkflowMain.java                     # Supervisor-based workflow
    └── DroolsWorkflowOrchestratorExampleMain.java  # Sequential workflow orchestration
```

## Available Examples

### 1. **DRLAuthoringLoopExampleMain**
- **Purpose**: Demonstrates iterative DRL authoring with guaranteed working output
- **Pattern**: Loop-based workflow with validation and execution cycles (uses DRLAuthoringAgent.createLoopWorkflow)
- **Usage**: `mvn exec:java -Dexec.mainClass="org.drools.agentic.example.main.DRLAuthoringLoopExampleMain"`

### 2. **DRLHybridLoopExampleMain**
- **Purpose**: Shows hybrid approach combining AI agents with deterministic services
- **Pattern**: AI for generation/execution, non-AI for fast validation
- **Usage**: `mvn exec:java -Dexec.mainClass="org.drools.agentic.example.main.DRLHybridLoopExampleMain"`

### 3. **DroolsWorkflowMain**
- **Purpose**: Demonstrates supervisor-based workflow coordination
- **Pattern**: Single model used for both planning and code generation
- **Usage**: `mvn exec:java -Dexec.mainClass="org.drools.agentic.example.main.DroolsWorkflowMain"`

### 4. **DroolsWorkflowOrchestratorExampleMain**
- **Purpose**: Sequential workflow with multiple specialized agents
- **Pattern**: Authoring → Storage → Knowledge Base pipeline
- **Usage**: `mvn exec:java -Dexec.mainClass="org.drools.agentic.example.main.DroolsWorkflowOrchestratorExampleMain"`

## Model Selection

All examples use the centralized `ModelSelector` utility that supports:

### Supported Models
- **Anthropic Claude Haiku** - Cloud-based with excellent reasoning
- **Granite 3.3 8B Instruct** - IBM's instruct-tuned model for planning with enhanced reasoning  
- **Qwen3 14B** - Latest generation with enhanced reasoning and code generation
- **Granite3 MoE 3B** - Lightweight with tool support

### Command Line Options
```bash
# Model selection
mvn exec:java -Dexec.mainClass="..." -Dexec.args="--granite"      # Granite 3.3 8B Instruct
mvn exec:java -Dexec.mainClass="..." -Dexec.args="--qwen-coder"   # Qwen3 14B
mvn exec:java -Dexec.mainClass="..." -Dexec.args="--anthropic"    # Anthropic Claude
mvn exec:java -Dexec.mainClass="..." -Dexec.args="--help"         # Show available models

# Custom models
mvn exec:java -Dexec.mainClass="..." -Dexec.args="--model=custom-model"
mvn exec:java -Dexec.mainClass="..." -Dexec.args="--ollama-url http://server:11434 model-name"
```

### Environment Variables
```bash
export MODEL_TYPE=QWEN_CODER          # Set default model
export ANTHROPIC_API_KEY=your_key     # Enable Anthropic models
export OLLAMA_BASE_URL=http://server  # Custom Ollama server
```

## Dependencies

This module depends on:
- `drools-agentic-workflow` - Core workflow agents and services
- `langchain4j-agentic` - Agent orchestration framework
- `langchain4j-anthropic` - Anthropic model integration
- `langchain4j-ollama` - Local Ollama model integration

## Development Commands

```bash
# Build examples module
mvn clean compile -pl drools-agentic-examples

# Run specific example
mvn exec:java -pl drools-agentic-examples -Dexec.mainClass="org.drools.agentic.example.main.DRLHybridLoopExampleMain"

# Run with arguments
mvn exec:java -pl drools-agentic-examples -Dexec.args="--granite --help"

# Build all modules
mvn clean compile
```

## Script Integration

The examples are integrated with the project's run scripts:
- `run-drools-agent.sh` - Uses examples from this module
- `run-drools-workflow-orchestrator.sh` - Points to examples in this module

## Architecture Benefits

### Separation of Concerns
- **Examples Module**: Executable demonstrations and model selection
- **Workflow Module**: Core agents, services, and orchestration logic
- **Clean Dependencies**: Examples depend on workflow, not vice versa

### Consistency
- All examples follow `ExampleMain` naming convention
- Centralized model selection via `ModelSelector`
- Unified command-line interface across all examples
- Consistent --help support and documentation