#!/bin/bash
# Simple output cleaner for workflow orchestrator
# Usage: ./run-drools-workflow-orchestrator.sh | ./clean-output.sh

grep -E '(🚀|🤖|✅|❌|📄|⚙️|🎯|═══|Agent (Started|Completed)|WORKFLOW|Final Result|ERROR|Exception)' \
| grep -v 'TRACE:' \
| grep -v 'DEBUG:' \
| grep -v 'SLF4J:' \
| sed 's/.*] //' \
| sed 's/org\.drools\.agentic\.example\.[^.]*\.//'