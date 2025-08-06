# Drools Agentic MCP Server

A Quarkus-based MCP (Model Context Protocol) server that provides AI assistants with sophisticated Drools rule development and execution capabilities.

## Features

### 🤖 AI-Powered Rule Development
- **improveKnowledgeBase**: Uses MainWorkflow multi-agent system to autonomously create and modify Drools rules
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
| `improveKnowledgeBase` | Create/modify rules using AI agents | `requirements` (string) |
| `executeRules` | Execute rules with JSON facts | `jsonFacts` (string), `maxActivations` (int) |
| `executeBatch` | Batch execute with multiple fact sets | `jsonFactBatches` (string), `maxActivations` (int) |
| `clearFacts` | Clear all facts from session | none |
| `getKnowledgeBaseStatus` | Get knowledge base status | none |

## Architecture

- **MainWorkflow**: Multi-agent orchestration system
  - DRL Authoring Agent: Generates and validates DRL code
  - File Storage Agent: Manages file operations
  - Knowledge Base Agent: Handles knowledge base building and management

- **KnowledgeRunnerService**: Non-AI rule execution service
  - Uses shared knowledge base storage
  - Deterministic rule processing
  - Session management and fact handling

## Usage Example

```json
// 1. Create rules using AI
{
  "tool": "improveKnowledgeBase",
  "parameters": {
    "requirements": "Create a Person rule that checks if age > 18 for adult classification"
  }
}

// 2. Check knowledge base status
{
  "tool": "getKnowledgeBaseStatus"
}

// 3. Execute rules with facts
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