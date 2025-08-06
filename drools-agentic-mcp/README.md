# Drools Agentic MCP Server

A Quarkus-based MCP (Model Context Protocol) server that provides AI assistants with sophisticated Drools rule development and execution capabilities.

## Features

### 🤖 AI-Powered Business Logic Development
- **improveKnowledgeBase**: Analyzes comprehensive domain specifications and implements sophisticated decision-making logic
- Takes detailed requirements including domain models, constraints, examples, and business rules
- Uses DroolsWorkflowOrchestrator multi-agent system for autonomous implementation
- Integrates DRL authoring, file storage, and knowledge base management agents
- Supports Ollama models (Granite, Granite3-MoE) for planning and code generation

### ⚡ Rule Execution
- **executeRules**: Execute rules with JSON facts using shared knowledge base
- **executeBatch**: Process multiple fact sets in batch mode
- **clearFacts**: Clear facts from the active session
- **getKnowledgeBaseStatus**: Get detailed status of the shared knowledge base

## Available MCP Tools

| Tool | Description | Parameters |
|------|-------------|------------|
| `improveKnowledgeBase` | Implement business logic from detailed specifications | `specification` (comprehensive design document) |
| `executeRules` | Execute rules with JSON facts | `jsonFacts` (string), `maxActivations` (int) |
| `executeBatch` | Batch execute with multiple fact sets | `jsonFactBatches` (string), `maxActivations` (int) |
| `clearFacts` | Clear all facts from session | none |
| `getKnowledgeBaseStatus` | Get knowledge base status | none |

## Available MCP Prompts

| Prompt | Description | Parameters |
|--------|-------------|------------|
| `businessLogicSpecificationGuide` | Generate comprehensive guide for creating business logic specifications | `domain` (business domain/use case) |
| `jsonFactsGuide` | Generate properly formatted JSON facts for rule execution with examples | `entityTypes` (domain entities to create facts for) |
| `batchExecutionGuide` | Generate batch execution scenarios for comprehensive testing | `scenario` (testing scenario/use case) |
| `knowledgeBaseStatusGuide` | Guide for interpreting status output and troubleshooting issues | none |
| `sessionManagementGuide` | Guide for session management and fact lifecycle best practices | none |
| `workflowGuide` | Generate complete end-to-end workflow examples from specification to execution | `workflowType` (business process type) |

## Architecture

- **DroolsWorkflowOrchestrator**: Multi-agent orchestration system
  - DRL Authoring Agent: Generates and validates DRL code
  - File Storage Agent: Manages file operations
  - Knowledge Base Agent: Handles knowledge base building and management

- **KnowledgeRunnerService**: Non-AI rule execution service
  - Uses shared knowledge base storage
  - Deterministic rule processing
  - Session management and fact handling

## Usage Example

```json
// 1. Get specification guide for your domain
{
  "prompt": "businessLogicSpecificationGuide",
  "parameters": {
    "domain": "e-commerce"
  }
}

// 2. Implement business logic from detailed specification
{
  "tool": "improveKnowledgeBase", 
  "parameters": {
    "specification": "Domain Model: Person entity with name (string), age (integer), email (string) attributes. Business Rules: 1) Classify as adult if age >= 18, 2) Email must contain @ symbol, 3) Premium customers get 10% discount if order > $100. Constraints: Age non-negative, name not empty. Examples: Person(name='John', age=25, email='john@example.com') with Order(amount=150) should receive premium discount."
  }
}

// 3. Check knowledge base status
{
  "tool": "getKnowledgeBaseStatus"
}

// 4. Execute rules with facts
{
  "tool": "executeRules", 
  "parameters": {
    "jsonFacts": "[{\"name\":\"John\", \"age\":25}]",
    "maxActivations": 0
  }
}
```

## Development

### Running the Server
```bash
mvn quarkus:dev -pl drools-agentic-mcp
```

### Testing
```bash
mvn test -pl drools-agentic-mcp
```

## Dependencies

- Quarkus MCP Server (stdio transport)
- LangChain4j for AI agent orchestration  
- Drools for rule engine
- Jackson for JSON processing
- Ollama for local LLM models

## Configuration

The server uses these Ollama models by default:
- **Planning**: `granite-code:20b` (supervisor/coordination)
- **Code Generation**: `granite3-moe:3b` (DRL authoring with tool support)

Ensure Ollama is running locally with these models available.