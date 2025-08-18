package org.drools.validation;

import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;

import java.util.ArrayList;
import java.util.List;

public class DrlFaultFinder {
    
    public static class FaultLocation {
        private final String faultyContent;
        private final int lineNumber;
        private final String errorMessage;
        
        public FaultLocation(String faultyContent, int lineNumber, String errorMessage) {
            this.faultyContent = faultyContent;
            this.lineNumber = lineNumber;
            this.errorMessage = errorMessage;
        }
        
        public String getFaultyContent() { return faultyContent; }
        public int getLineNumber() { return lineNumber; }
        public String getErrorMessage() { return errorMessage; }
        
        @Override
        public String toString() {
            return "Fault found at line " + lineNumber + ": " + errorMessage + "\nFaulty content: " + faultyContent;
        }
    }
    
    public FaultLocation findFaultyLine(String drlContent) {
        if (drlContent == null || drlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("DRL content cannot be null or empty");
        }
        
        if (isValidDrl(drlContent)) {
            return null;
        }
        
        String[] lines = drlContent.split("\n");
        return findFaultyLineByBinarySearch(lines);
    }
    
    private FaultLocation findFaultyLineByBinarySearch(String[] lines) {
        if (lines.length == 1) {
            String content = lines[0].trim();
            String errorMsg = getCompilationError(content);
            return new FaultLocation(content, 1, errorMsg);
        }
        
        return binarySearchForFaultyLine(lines, 0, lines.length - 1);
    }
    
    private FaultLocation binarySearchForFaultyLine(String[] lines, int start, int end) {
        if (start == end) {
            String content = lines[start].trim();
            String errorMsg = getCompilationError(rebuildDrlFromLines(lines, 0, start));
            return new FaultLocation(content, start + 1, errorMsg);
        }
        
        int mid = start + (end - start) / 2;
        String testDrl = rebuildDrlFromLines(lines, 0, mid);
        
        if (isValidDrl(testDrl)) {
            if (mid + 1 <= end) {
                return binarySearchForFaultyLine(lines, mid + 1, end);
            } else {
                String content = lines[end].trim();
                String errorMsg = getCompilationError(rebuildDrlFromLines(lines, 0, end));
                return new FaultLocation(content, end + 1, errorMsg);
            }
        } else {
            return binarySearchForFaultyLine(lines, start, mid);
        }
    }
    
    private String rebuildDrlFromLines(String[] lines, int startIndex, int endIndex) {
        StringBuilder sb = new StringBuilder();
        boolean inRule = false;
        boolean inQuery = false;
        boolean inFunction = false;
        boolean inDeclare = false;
        boolean hasWhen = false;
        boolean hasThen = false;
        int braceCount = 0;
        
        for (int i = startIndex; i <= endIndex && i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = line.trim().toLowerCase();
            
            // Track rule structure
            if (trimmedLine.startsWith("rule ")) {
                inRule = true;
                hasWhen = false;
                hasThen = false;
            } else if (inRule && trimmedLine.equals("when")) {
                hasWhen = true;
            } else if (inRule && trimmedLine.equals("then")) {
                hasThen = true;
            } else if (inRule && trimmedLine.equals("end")) {
                inRule = false;
            }
            // Track query structure
            else if (trimmedLine.startsWith("query ")) {
                inQuery = true;
            } else if (inQuery && trimmedLine.equals("end")) {
                inQuery = false;
            }
            // Track function structure
            else if (trimmedLine.startsWith("function ")) {
                inFunction = true;
                braceCount = 0;
            } else if (inFunction) {
                braceCount += countChar(line, '{') - countChar(line, '}');
                if (braceCount == 0 && (line.contains("{") || line.contains("}"))) {
                    inFunction = false;
                }
            }
            // Track declare structure
            else if (trimmedLine.startsWith("declare ")) {
                inDeclare = true;
            } else if (inDeclare && trimmedLine.equals("end")) {
                inDeclare = false;
            }
            
            sb.append(line);
            if (i < endIndex) {
                sb.append("\n");
            }
        }
        
        // Complete incomplete structures
        if (inRule) {
            if (!hasWhen) {
                sb.append("\nwhen");
            }
            if (!hasThen) {
                sb.append("\nthen");
            }
            sb.append("\nend");
        } else if (inQuery) {
            sb.append("\nend");
        } else if (inFunction) {
            // Add closing braces for incomplete function
            while (braceCount > 0) {
                sb.append("\n}");
                braceCount--;
            }
            if (braceCount == 0 && !sb.toString().trim().endsWith("}")) {
                sb.append("\n{ }");
            }
        } else if (inDeclare) {
            sb.append("\nend");
        }
        
        return sb.toString();
    }
    
    private int countChar(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }
    
    private boolean isValidDrl(String drlContent) {
        if (drlContent == null || drlContent.trim().isEmpty()) {
            return true;
        }
        
        try {
            KieHelper kieHelper = new KieHelper();
            kieHelper.addContent(drlContent, ResourceType.DRL);
            return !kieHelper.verify().hasMessages(Message.Level.ERROR);
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getCompilationError(String drlContent) {
        try {
            KieHelper kieHelper = new KieHelper();
            kieHelper.addContent(drlContent, ResourceType.DRL);
            
            List<Message> errors = kieHelper.verify().getMessages(Message.Level.ERROR);
            if (!errors.isEmpty()) {
                return errors.get(0).getText();
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        
        return "Unknown compilation error";
    }
}