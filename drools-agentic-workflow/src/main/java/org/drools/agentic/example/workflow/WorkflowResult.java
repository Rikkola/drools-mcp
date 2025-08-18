package org.drools.agentic.example.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Standardized result format for workflow execution with comprehensive reporting.
 */
public class WorkflowResult {
    private final String status;
    private final Map<String, Object> phaseResults;
    private final WorkflowMetrics metrics;
    private final List<String> warnings;
    private final List<String> errors;
    private final String finalOutput;
    private final Duration totalDuration;
    private final Map<String, Duration> phaseDurations;
    
    public WorkflowResult(WorkflowState state, WorkflowMetrics metrics, String finalOutput) {
        this.status = state.getStatus();
        this.phaseResults = state.getPhaseResults();
        this.metrics = metrics;
        this.warnings = state.getWarnings();
        this.errors = state.getErrors();
        this.finalOutput = finalOutput;
        this.totalDuration = state.getTotalDuration();
        this.phaseDurations = extractPhaseDurations(state);
    }
    
    private Map<String, Duration> extractPhaseDurations(WorkflowState state) {
        return state.getCompletedPhases().stream()
            .collect(java.util.stream.Collectors.toMap(
                phase -> phase,
                phase -> state.getPhaseDuration(phase) != null ? 
                        state.getPhaseDuration(phase) : Duration.ZERO
            ));
    }
    
    public String formatReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=".repeat(70)).append("\n");
        report.append("🎯 WORKFLOW EXECUTION REPORT\n");
        report.append("=".repeat(70)).append("\n");
        
        // Status and timing
        report.append(String.format("📊 Status: %s%n", status));
        report.append(String.format("⏱️  Total Duration: %dms%n", totalDuration.toMillis()));
        
        // Phase breakdown
        if (!phaseResults.isEmpty()) {
            report.append("\n📋 PHASE EXECUTION:\n");
            phaseResults.forEach((phase, result) -> {
                Duration phaseDuration = phaseDurations.getOrDefault(phase, Duration.ZERO);
                report.append(String.format("   ✅ %-20s: %dms%n", phase, phaseDuration.toMillis()));
            });
        }
        
        // Warnings
        if (!warnings.isEmpty()) {
            report.append(String.format("%n⚠️  WARNINGS (%d):%n", warnings.size()));
            warnings.forEach(warning -> 
                report.append(String.format("   • %s%n", warning)));
        }
        
        // Errors
        if (!errors.isEmpty()) {
            report.append(String.format("%n❌ ERRORS (%d):%n", errors.size()));
            errors.forEach(error -> 
                report.append(String.format("   • %s%n", error)));
        }
        
        // Performance summary
        if (metrics != null) {
            double successRate = metrics.getSuccessRate();
            if (successRate >= 0) {
                report.append(String.format("%n📈 SUCCESS RATE: %.1f%%%n", successRate));
            }
            
            Map<String, Double> phaseAverages = metrics.getPhaseAverages();
            if (!phaseAverages.isEmpty()) {
                report.append("\n📊 PHASE AVERAGES:\n");
                phaseAverages.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> 
                        report.append(String.format("   %-20s: %.1fms%n", 
                            entry.getKey(), entry.getValue())));
            }
        }
        
        // Final output
        if (finalOutput != null && !finalOutput.trim().isEmpty()) {
            report.append("\n📄 FINAL OUTPUT:\n");
            if (finalOutput.length() > 500) {
                report.append(finalOutput.substring(0, 500)).append("...\n");
                report.append(String.format("   (Total length: %d characters)%n", finalOutput.length()));
            } else {
                report.append(finalOutput).append("\n");
            }
        }
        
        report.append("=".repeat(70));
        
        return report.toString();
    }
    
    public String formatSummary() {
        return String.format(
            "Workflow %s in %dms | Phases: %d | Warnings: %d | Errors: %d",
            status.toLowerCase(), totalDuration.toMillis(), 
            phaseResults.size(), warnings.size(), errors.size()
        );
    }
    
    public boolean isSuccessful() {
        return "COMPLETED".equals(status) || "COMPLETED_WITH_ERRORS".equals(status);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    // Getters
    public String getStatus() { return status; }
    public Map<String, Object> getPhaseResults() { return phaseResults; }
    public WorkflowMetrics getMetrics() { return metrics; }
    public List<String> getWarnings() { return warnings; }
    public List<String> getErrors() { return errors; }
    public String getFinalOutput() { return finalOutput; }
    public Duration getTotalDuration() { return totalDuration; }
    public Map<String, Duration> getPhaseDurations() { return phaseDurations; }
    
    @Override
    public String toString() {
        return formatSummary();
    }
}