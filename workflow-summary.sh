#!/bin/bash
# Workflow Summary - Shows only key milestones
# Usage: ./run-drools-workflow-orchestrator.sh | ./workflow-summary.sh

echo "🎯 Workflow Summary:"
echo "─────────────────────"

awk '
BEGIN { 
    agents = 0
    workflow_started = 0
}

# Track workflow start
/🚀 ENHANCED DROOLS WORKFLOW/ { 
    workflow_started = 1
    print "🚀 Enhanced workflow started"
}

# Track agent creation
/Agent Started:/ { 
    agents++
    match($0, /Agent Started: ([^|]+)/, arr)
    if (arr[1]) {
        printf "🤖 [%d] %s\n", agents, arr[1]
    }
}

# Track agent completion
/Agent Completed:.*Duration: ([0-9]+ms)/ {
    match($0, /Duration: ([^|]+)/, arr)
    if (arr[1]) {
        printf "✅ [%d] Completed in %s\n", agents, arr[1]
    }
}

# Track major phases
/DRL.*generated|Generating.*DRL/ { print "📝 DRL generation in progress" }
/Saving.*file|File.*saved/ { print "💾 File operations completed" }
/Knowledge.*base|Compiling.*knowledge/ { print "🧠 Knowledge base processing" }

# Track completion
/✅.*WORKFLOW COMPLETED/ { print "✅ Workflow completed successfully" }
/📄 Final Result:/ { print "📄 Final result generated" }

# Track errors
/❌|ERROR:|Exception|FAILED/ { 
    gsub(/.*ERROR:/, "❌ ERROR:")
    gsub(/.*Exception/, "❌ Exception")
    print $0
}

END {
    if (workflow_started) {
        print "─────────────────────"
        printf "📊 Summary: %d agents processed\n", agents
    }
}'