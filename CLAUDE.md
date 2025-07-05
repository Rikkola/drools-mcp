# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Drools MCP (Model Context Protocol) server built with Quarkus. It provides AI assistants with tools to execute, validate, and manage Drools DRL (Decision Rule Language) code. The project serves as a bridge between AI models and the Drools rule engine.

## Architecture

The project follows a modular architecture with clear separation of concerns:

### Core Components

- **DRLTool** (`src/main/java/org/drools/DRLTool.java`): Main MCP tool provider class that exposes all available tools to AI clients
- **DRLRunner** (`src/main/java/org/drools/DRLRunner.java`): Executes DRL code with or without external facts
- **DRLVerifier** (`src/main/java/org/drools/DRLVerifier.java`): Performs structural validation of DRL code using Drools verifier
- **DefinitionStorage** (`src/main/java/org/drools/DefinitionStorage.java`): Manages storage and retrieval of reusable DRL definitions (types, functions, etc.)
- **DynamicObjectFactory** (`src/main/java/org/drools/DynamicObjectFactory.java`): Creates dynamic objects from JSON data for use with Drools rules

### MCP Tools Available

The DRLTool class exposes these tools to AI clients:

1. **validateDRLStructure** - Validates DRL code structure
2. **runDRLCode** - Executes DRL with declared types and data creation rules
3. **runDRLWithExternalFacts** - Executes DRL with external JSON facts
4. **addDefinition** - Stores reusable DRL definitions
5. **getAllDefinitions** - Lists all stored definitions
6. **getDefinition** - Retrieves specific definition by name
7. **generateDRLFromDefinitions** - Generates complete DRL from stored definitions
8. **getDefinitionsSummary** - Gets summary of definitions grouped by type
9. **removeDefinition** - Removes stored definition

## Common Development Commands

### Build and Test
```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Run specific test
mvn test -Dtest=DRLRunnerTest

# Build uber JAR for jbang execution
mvn package

# Run in development mode
mvn quarkus:dev
```

### Native Build
```bash
# Build native executable
mvn package -Pnative

# Run native executable
./target/drools-mcp-1.0.0-SNAPSHOT-runner
```

### JBang Execution
```bash
# Run as MCP server via jbang
jbang --quiet org.drools:drools-mcp:1.0.0-SNAPSHOT:runner
```

## Key Dependencies

- **Quarkus 3.20.0** - Application framework
- **Drools 8.44.0.Final** - Rule engine (core, compiler, verifier)
- **KIE API 8.44.0.Final** - Knowledge base API
- **Quarkus MCP Server 1.0.0** - MCP protocol implementation
- **Jackson** - JSON processing for dynamic objects

## Configuration

The application uses these key configuration properties in `src/main/resources/application.properties`:

- `quarkus.package.jar.type=uber-jar` - Enables single JAR deployment
- `quarkus.log.file.enable=true` - Enables file logging to `drools-mcp.log`
- `quarkus.mcp.server.sse.root-path=/` - MCP server configuration

## Test Resources

The project includes comprehensive test DRL files in `src/test/resources/drl/` covering:
- Basic rule validation
- Counter and fact creation patterns
- Person age categorization examples
- Order discount logic
- Message handling with quotes
- Maximum rule execution limits

## Development Notes

- The project uses Java 17 as the target version
- All MCP tools return JSON-formatted responses for consistent AI integration
- The DefinitionStorage uses thread-safe ConcurrentHashMap for multi-threaded access
- DynamicObjectFactory uses Java Proxy pattern for creating runtime objects
- Extensive logging is enabled for debugging rule execution

## Claude Desktop Integration

To use this MCP server with Claude Desktop, add this configuration to your MCP settings:

```json
{
   "mcpServers": {
      "drl-verifier": {
         "command": "jbang",
         "args": ["--quiet", "org.drools:drools-mcp:1.0.0-SNAPSHOT:runner"]
      }
   }
}
```
