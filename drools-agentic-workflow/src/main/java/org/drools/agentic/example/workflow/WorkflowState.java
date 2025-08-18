package org.drools.agentic.example.workflow;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the state and progress of a workflow execution.
 * Provides comprehensive state management for agent workflows.
 */
public class WorkflowState {
    private String currentPhase;
    private final Map<String, Object> phaseResults = new ConcurrentHashMap<>();
    private final List<String> completedPhases = Collections.synchronizedList(new ArrayList<>());
    private final Instant startTime = Instant.now();
    private final Map<String, Duration> phaseDurations = new ConcurrentHashMap<>();
    private final Map<String, Instant> phaseStartTimes = new ConcurrentHashMap<>();
    private final List<String> errors = Collections.synchronizedList(new ArrayList<>());
    private final List<String> warnings = Collections.synchronizedList(new ArrayList<>());
    private String status = "INITIALIZING";

    public void startPhase(String phaseName) {
        this.currentPhase = phaseName;
        this.phaseStartTimes.put(phaseName, Instant.now());
        this.status = "RUNNING";
    }

    public void completePhase(String phaseName, Object result) {
        Instant startTime = phaseStartTimes.get(phaseName);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            phaseDurations.put(phaseName, duration);
        }
        
        phaseResults.put(phaseName, result);
        completedPhases.add(phaseName);
        
        // Check if this is the last phase
        if (isWorkflowComplete()) {
            this.status = errors.isEmpty() ? "COMPLETED" : "COMPLETED_WITH_ERRORS";
        }
    }

    public void addError(String phaseName, String error) {
        errors.add(String.format("[%s] %s", phaseName, error));
        if ("RUNNING".equals(status)) {
            this.status = "ERROR";
        }
    }

    public void addWarning(String phaseName, String warning) {
        warnings.add(String.format("[%s] %s", phaseName, warning));
    }

    public Duration getTotalDuration() {
        return Duration.between(startTime, Instant.now());
    }

    public Duration getPhaseDuration(String phaseName) {
        return phaseDurations.get(phaseName);
    }

    public boolean isWorkflowComplete() {
        // This would be configured based on the expected phases
        return completedPhases.size() >= 3; // DRL Authoring, File Storage, Knowledge Base
    }

    // Getters
    public String getCurrentPhase() { return currentPhase; }
    public Map<String, Object> getPhaseResults() { return new HashMap<>(phaseResults); }
    public List<String> getCompletedPhases() { return new ArrayList<>(completedPhases); }
    public List<String> getErrors() { return new ArrayList<>(errors); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
    public String getStatus() { return status; }
    public Instant getStartTime() { return startTime; }

    @Override
    public String toString() {
        return String.format(
            "WorkflowState{status='%s', currentPhase='%s', completed=%d, errors=%d, warnings=%d, duration=%dms}",
            status, currentPhase, completedPhases.size(), errors.size(), warnings.size(), 
            getTotalDuration().toMillis()
        );
    }
}