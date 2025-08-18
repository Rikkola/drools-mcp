package org.drools.agentic.example.workflow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time progress tracking with visual feedback for long-running operations.
 */
public class ProgressTracker {
    private final Map<String, ProgressState> phaseProgress = new ConcurrentHashMap<>();
    private boolean enableProgressBar = true;
    
    public static class ProgressState {
        private final int current;
        private final int total;
        private final String currentAction;
        private final long timestamp;
        
        public ProgressState(int current, int total, String currentAction) {
            this.current = current;
            this.total = total;
            this.currentAction = currentAction;
            this.timestamp = System.currentTimeMillis();
        }
        
        public int getCurrent() { return current; }
        public int getTotal() { return total; }
        public String getCurrentAction() { return currentAction; }
        public long getTimestamp() { return timestamp; }
        public int getPercentage() { return total > 0 ? (int) ((current * 100.0) / total) : 0; }
    }
    
    public void updateProgress(String phase, int current, int total, String currentAction) {
        phaseProgress.put(phase, new ProgressState(current, total, currentAction));
        
        if (enableProgressBar) {
            printProgressBar(phase, current, total, currentAction);
        }
    }
    
    public void completePhase(String phase) {
        ProgressState state = phaseProgress.get(phase);
        if (state != null) {
            updateProgress(phase, state.getTotal(), state.getTotal(), "Completed");
            System.out.println(); // New line after progress bar
        }
    }
    
    private void printProgressBar(String phase, int current, int total, String action) {
        if (total <= 0) {
            System.out.printf("\r🔄 %s: %s", phase, action);
            return;
        }
        
        int percentage = (int) ((current * 100.0) / total);
        int filledBars = percentage / 5; // 20 character progress bar
        int emptyBars = 20 - filledBars;
        
        String progressBar = "█".repeat(Math.max(0, filledBars)) + 
                           "░".repeat(Math.max(0, emptyBars));
        
        System.out.printf("\r🔄 %s: [%s] %d%% (%d/%d) - %s", 
            phase, progressBar, percentage, current, total, 
            truncateAction(action, 30));
        
        if (current >= total) {
            System.out.println(); // Complete the line when done
        }
    }
    
    public void reportSubProgress(String phase, String subTask) {
        System.out.printf("\n   ↳ %s: %s", phase, subTask);
    }
    
    public ProgressState getProgress(String phase) {
        return phaseProgress.get(phase);
    }
    
    public Map<String, ProgressState> getAllProgress() {
        return new ConcurrentHashMap<>(phaseProgress);
    }
    
    public void setEnableProgressBar(boolean enable) {
        this.enableProgressBar = enable;
    }
    
    private String truncateAction(String action, int maxLength) {
        if (action == null) return "";
        if (action.length() <= maxLength) return action;
        return action.substring(0, maxLength - 3) + "...";
    }
    
    public void reset() {
        phaseProgress.clear();
    }
}