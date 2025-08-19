#!/bin/bash

# Workflow Output Formatter
# Formats the workflow output for better readability
# Usage: ./run-drools-workflow-orchestrator.sh | ./format-workflow-output.sh [mode]
# Modes: summary (default), verbose, minimal

MODE=${1:-summary}

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
GRAY='\033[0;37m'
NC='\033[0m'

# Track workflow state
WORKFLOW_STARTED=false
CURRENT_PHASE=""
AGENT_COUNT=0

format_line() {
    local line="$1"
    
    # Skip empty lines in summary mode
    if [[ "$MODE" == "summary" && -z "$line" ]]; then
        return
    fi
    
    # Workflow start markers
    if [[ $line =~ "🚀 ENHANCED DROOLS WORKFLOW" ]]; then
        WORKFLOW_STARTED=true
        echo -e "${WHITE}${line}${NC}"
        echo -e "${BLUE}$(printf '─%.0s' {1..70})${NC}"
        return
    fi
    
    # Configuration and environment info
    if [[ $line =~ "🤖 Using.*model:" ]]; then
        echo -e "${CYAN}${line}${NC}"
        return
    fi
    
    if [[ $line =~ "⚙️.*configuration:" ]]; then
        echo -e "${PURPLE}${line}${NC}"
        return
    fi
    
    # Agent creation and completion
    if [[ $line =~ "Agent Started:" ]]; then
        AGENT_COUNT=$((AGENT_COUNT + 1))
        local agent_name=$(echo "$line" | sed -n 's/.*Agent Started: \([^|]*\).*/\1/p')
        echo -e "${BLUE}🤖 [$AGENT_COUNT] Starting $agent_name...${NC}"
        return
    fi
    
    if [[ $line =~ "Agent Completed:" ]]; then
        local duration=$(echo "$line" | sed -n 's/.*Duration: \([^|]*\).*/\1/p')
        echo -e "${GREEN}✅ [$AGENT_COUNT] Completed in $duration${NC}"
        return
    fi
    
    # Workflow phases
    if [[ $line =~ "Creating Enhanced Drools Workflow" ]]; then
        echo -e "${YELLOW}🔧 Initializing enhanced workflow orchestrator...${NC}"
        return
    fi
    
    # DRL generation progress
    if [[ $line =~ "DRL.*generated" || $line =~ "Generating.*DRL" ]]; then
        echo -e "${CYAN}📝 $line${NC}"
        return
    fi
    
    # File operations
    if [[ $line =~ "Saving.*file" || $line =~ "File.*saved" ]]; then
        echo -e "${BLUE}💾 $line${NC}"
        return
    fi
    
    # Knowledge base operations
    if [[ $line =~ "Knowledge.*base" || $line =~ "Compiling.*knowledge" ]]; then
        echo -e "${PURPLE}🧠 $line${NC}"
        return
    fi
    
    # Success indicators
    if [[ $line =~ "✅.*WORKFLOW COMPLETED" ]]; then
        echo -e "${BLUE}$(printf '─%.0s' {1..70})${NC}"
        echo -e "${GREEN}${line}${NC}"
        return
    fi
    
    if [[ $line =~ "📄 Final Result:" ]]; then
        echo -e "${WHITE}${line}${NC}"
        return
    fi
    
    # Error patterns
    if [[ $line =~ "❌" || $line =~ "ERROR:" || $line =~ "Exception" || $line =~ "FAILED" ]]; then
        echo -e "${RED}${line}${NC}"
        return
    fi
    
    # Warning patterns
    if [[ $line =~ "⚠️" || $line =~ "WARN:" || $line =~ "WARNING" ]]; then
        echo -e "${YELLOW}${line}${NC}"
        return
    fi
    
    # Progress indicators
    if [[ $line =~ "🔄" || $line =~ "⏳" || $line =~ "progress" ]]; then
        echo -e "${CYAN}${line}${NC}"
        return
    fi
    
    # HTTP requests (show only in verbose mode)
    if [[ $line =~ "HTTP request:" ]]; then
        if [[ "$MODE" == "verbose" ]]; then
            echo -e "${GRAY}🌐 HTTP request sent${NC}"
        fi
        return
    fi
    
    # Debug traces (show only in verbose mode)
    if [[ $line =~ "TRACE:" || $line =~ "DEBUG:" ]]; then
        if [[ "$MODE" == "verbose" ]]; then
            echo -e "${GRAY}${line}${NC}"
        fi
        return
    fi
    
    # Maven output filtering
    if [[ $line =~ "INFO.*maven" || $line =~ "\[INFO\]" ]]; then
        if [[ "$MODE" == "verbose" ]]; then
            echo -e "${GRAY}${line}${NC}"
        fi
        return
    fi
    
    # Default: show important lines in summary mode
    if [[ "$MODE" == "summary" ]]; then
        # Show lines that seem important
        if [[ $line =~ [🚀📊🤖✅❌⚠️📄🎯═─] || $line =~ "Result" || $line =~ "Complete" || $line =~ "Failed" ]]; then
            echo "$line"
        fi
    elif [[ "$MODE" == "minimal" ]]; then
        # Show only major milestones
        if [[ $line =~ [🚀✅❌📄] ]]; then
            echo "$line"
        fi
    else
        # Verbose mode: show everything
        echo "$line"
    fi
}

# Process input line by line
while IFS= read -r line; do
    format_line "$line"
done

# Add final separator if workflow was detected
if [[ "$WORKFLOW_STARTED" == "true" ]]; then
    echo -e "${BLUE}$(printf '═%.0s' {1..70})${NC}"
fi