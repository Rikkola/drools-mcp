# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-module Maven project for Drools AI POC (Proof of Concept) tools. The project is structured with a parent POM and multiple submodules to provide AI assistants with tools to work with Drools and other rule engines.

## Project Structure

```
drools-ai-poc/
├── pom.xml (parent POM - drools-ai-poc)
├── CLAUDE.md (this file)
└── knowledge-builder-mcp/ (submodule)
    ├── pom.xml
    ├── CLAUDE.md
    └── src/
```

## Modules

### knowledge-builder-mcp
A Drools MCP server built with Quarkus that provides AI assistants with tools to execute, validate, and manage Drools DRL (Decision Rule Language) code. See `knowledge-builder-mcp/CLAUDE.md` for detailed documentation.

## Common Development Commands

### Build All Modules
```bash
# Build the entire project from root
mvn clean compile

# Build specific module
mvn clean compile -pl knowledge-builder-mcp

# Run tests for all modules
mvn test

# Run tests for specific module
mvn test -pl knowledge-builder-mcp

# Package all modules
mvn package
```

### Module-Specific Commands
For module-specific commands, either:
1. Navigate to the module directory and run Maven commands
2. Use the `-pl` (projects list) option from the root

## Adding New Modules

To add a new module:
1. Create the module directory
2. Add `<module>module-name</module>` to the parent POM
3. Create the module's POM with proper parent reference
4. Add module-specific documentation to the module's CLAUDE.md

## Architecture Notes

- The parent POM manages common properties, dependency management, and plugin configuration
- Each submodule inherits from the parent and can override/extend as needed
- Shared dependencies and versions are managed at the parent level
- Module-specific dependencies are defined in each module's POM