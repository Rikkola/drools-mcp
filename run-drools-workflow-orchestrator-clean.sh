#!/bin/bash

# Enhanced DroolsWorkflowOrchestrator Runner Script with Clean Output
# Usage: ./run-drools-workflow-orchestrator-clean.sh [options]
# Options:
#   --verbose       Show all output including debug logs
#   --quiet         Minimal output, errors only
#   --summary       Show workflow summary only (default)
#   --no-progress   Disable progress bars
#   --no-colors     Disable color output

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Parse output control arguments
VERBOSE=false
QUIET=false
SUMMARY=true
NO_PROGRESS=false
NO_COLORS=false
WORKFLOW_ARGS=()

for arg in "$@"; do
    case $arg in
        --verbose)
            VERBOSE=true
            SUMMARY=false
            ;;
        --quiet)
            QUIET=true
            SUMMARY=false
            WORKFLOW_ARGS+=(--quiet)
            ;;
        --summary)
            SUMMARY=true
            ;;
        --no-progress)
            NO_PROGRESS=true
            WORKFLOW_ARGS+=(--no-progress)
            ;;
        --no-colors)
            NO_COLORS=true
            ;;
        *)
            WORKFLOW_ARGS+=("$arg")
            ;;
    esac
done

# Color definitions (disable if --no-colors)
if [ "$NO_COLORS" = false ]; then
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    BLUE='\033[0;34m'
    PURPLE='\033[0;35m'
    CYAN='\033[0;36m'
    WHITE='\033[1;37m'
    NC='\033[0m' # No Color
else
    RED=''
    GREEN=''
    YELLOW=''
    BLUE=''
    PURPLE=''
    CYAN=''
    WHITE=''
    NC=''
fi

echo -e "${CYAN}🚀 Running Enhanced Drools Workflow Orchestrator${NC}"
echo -e "${BLUE}📍 Working directory: $SCRIPT_DIR${NC}"

# Check if project is built
if [ ! -d "drools-agentic-examples/target/classes" ]; then
    echo -e "${YELLOW}🔨 Building project...${NC}"
    mvn clean compile -q
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Build failed!${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Build successful!${NC}"
fi

# Display configuration
if [ "$QUIET" = false ]; then
    echo -e "${PURPLE}⚙️  Configuration:${NC}"
    if [ "$VERBOSE" = true ]; then
        echo -e "   ${CYAN}• Output Mode: VERBOSE (all logs)${NC}"
    elif [ "$QUIET" = true ]; then
        echo -e "   ${CYAN}• Output Mode: QUIET (errors only)${NC}"
    else
        echo -e "   ${CYAN}• Output Mode: SUMMARY (workflow progress only)${NC}"
    fi
    echo -e "   ${CYAN}• Arguments: ${WORKFLOW_ARGS[*]}${NC}"
    
    echo -e "${PURPLE}🌍 Environment:${NC}"
    [ -n "$ANTHROPIC_API_KEY" ] && echo -e "   ${GREEN}• ANTHROPIC_API_KEY: configured${NC}" || echo -e "   ${YELLOW}• ANTHROPIC_API_KEY: (not set)${NC}"
    [ -n "$OLLAMA_MODEL" ] && echo -e "   ${GREEN}• OLLAMA_MODEL: $OLLAMA_MODEL${NC}" || echo -e "   ${YELLOW}• OLLAMA_MODEL: (not set)${NC}"
    [ -n "$OLLAMA_BASE_URL" ] && echo -e "   ${GREEN}• OLLAMA_BASE_URL: $OLLAMA_BASE_URL${NC}" || echo -e "   ${YELLOW}• OLLAMA_BASE_URL: (not set)${NC}"
    echo ""
fi

# Prepare output filtering based on mode
if [ "$VERBOSE" = true ]; then
    # Show everything
    FILTER_CMD="cat"
elif [ "$QUIET" = true ]; then
    # Show only errors and final result
    FILTER_CMD="grep -E '(❌|ERROR:|FAILED|Exception|✅.*WORKFLOW COMPLETED|📄 Final Result)' --line-buffered || true"
else
    # Summary mode - show workflow progress, agent interactions, and key results
    FILTER_CMD="grep -E '(🚀|📊|🤖|✅|❌|⚠️|📄|🎯|═══|───|Agent Started|Agent Completed|WORKFLOW|ERROR|Exception|Final Result|SUCCESS)' --line-buffered | grep -v 'TRACE:' || true"
fi

echo -e "${WHITE}🎯 Starting Enhanced Workflow Orchestrator...${NC}"
echo -e "${BLUE}$(printf '═%.0s' {1..60})${NC}"

# Run with appropriate filtering
mvn exec:java -pl drools-agentic-examples \
    -Dexec.mainClass="org.drools.agentic.example.main.DroolsWorkflowOrchestratorExampleMain" \
    -Dexec.args="${WORKFLOW_ARGS[*]}" \
    -q 2>&1 | eval "$FILTER_CMD"

EXIT_CODE=${PIPESTATUS[0]}

echo -e "${BLUE}$(printf '═%.0s' {1..60})${NC}"

if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}🏁 Enhanced Workflow Orchestrator completed successfully!${NC}"
else
    echo -e "${RED}❌ Enhanced Workflow Orchestrator failed with exit code $EXIT_CODE${NC}"
fi

echo -e "${CYAN}💡 Tip: Use --verbose for full logs, --quiet for minimal output, --summary for balanced view${NC}"

exit $EXIT_CODE