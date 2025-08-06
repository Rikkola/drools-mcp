#!/bin/bash

# MainWorkflow Runner Script
# Usage: ./run-main-agent.sh [options]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "🚀 Running Main Workflow Example..."
echo "📍 Working directory: $SCRIPT_DIR"

# Check if project is built
if [ ! -d "knowledge-mcp/target/classes" ]; then
    echo "🔨 Building project..."
    mvn clean compile -q
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
    echo "✅ Build successful!"
fi

# Run the agent
echo "🎯 Starting MainWorkflowRunner..."
echo "📋 Arguments: $*"
echo "🌍 Environment variables:"
[ -n "$ANTHROPIC_API_KEY" ] && echo "   - ANTHROPIC_API_KEY: ****" || echo "   - ANTHROPIC_API_KEY: (not set)"
[ -n "$OLLAMA_MODEL" ] && echo "   - OLLAMA_MODEL: $OLLAMA_MODEL" || echo "   - OLLAMA_MODEL: (not set)"
[ -n "$OLLAMA_BASE_URL" ] && echo "   - OLLAMA_BASE_URL: $OLLAMA_BASE_URL" || echo "   - OLLAMA_BASE_URL: (not set)"
echo ""

mvn exec:java -pl knowledge-mcp \
    -Dexec.mainClass="org.drools.agentic.example.main.MainWorkflowRunner" \
    -Dexec.args="$*" \
    -q

echo ""
echo "🏁 MainWorkflow execution completed."