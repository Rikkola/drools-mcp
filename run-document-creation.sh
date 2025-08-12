#!/bin/bash

# Document Planning Agent Runner Script
# Usage: ./run-document-creation.sh [options]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📄 Running Document Planning Agent Demo..."
echo "📍 Working directory: $SCRIPT_DIR"

# Check if project is built
if [ ! -d "drools-agentic-examples/target/classes" ]; then
    echo "🔨 Building project..."
    mvn clean compile -q
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
    echo "✅ Build successful!"
fi

# Run the agent
echo "🎯 Starting Document Planning Agent..."
echo "📋 Arguments: $*"
echo "🌍 Environment variables:"
[ -n "$ANTHROPIC_API_KEY" ] && echo "   - ANTHROPIC_API_KEY: ****" || echo "   - ANTHROPIC_API_KEY: (not set)"
[ -n "$OLLAMA_MODEL" ] && echo "   - OLLAMA_MODEL: $OLLAMA_MODEL" || echo "   - OLLAMA_MODEL: (not set)"
[ -n "$OLLAMA_BASE_URL" ] && echo "   - OLLAMA_BASE_URL: $OLLAMA_BASE_URL" || echo "   - OLLAMA_BASE_URL: (not set)"
echo ""

# Capture output and display it properly
echo "🔄 Executing document planning agent..."
echo ""

OUTPUT=$(mvn exec:java -pl drools-agentic-examples \
    -Dexec.mainClass="org.drools.agentic.example.main.DocumentPlanningExampleMain" \
    -Dexec.args="$*" \
    -q 2>&1)

# Print the full output
echo "$OUTPUT"

echo ""
echo "📄 =============================================="
echo "📄 GENERATED DOCUMENT ANALYSIS (FINAL OUTPUT)"  
echo "📄 =============================================="
echo ""

# Extract and highlight just the analysis result (everything after "--- Analysis Output ---")
echo "$OUTPUT" | sed -n '/--- Analysis Output ---/,$p' | tail -n +2 | sed '/=== Document Analysis Completed ===/,$d'

echo ""
echo "🏁 Document planning analysis completed."