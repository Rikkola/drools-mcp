package org.drools.execution;

import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Core DRL execution engine responsible for compiling DRL content,
 * managing KieSession lifecycle, and executing rules.
 */
public class DRLExecutor {

    /**
     * Executes DRL content with the provided facts
     * @param drlContent The DRL content to compile and execute
     * @param facts List of facts to insert into the session
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return DRLRunnerResult containing execution results
     * @throws RuntimeException if DRL compilation or execution fails
     */
    public DRLRunnerResult execute(String drlContent, List<Object> facts, int maxRuns) {
        validateInput(drlContent, maxRuns);
        
        KieContainer kieContainer = buildKieContainer(drlContent);
        return executeWithContainer(kieContainer, facts, maxRuns);
    }

    /**
     * Executes with a pre-built KieContainer
     * @param kieContainer Pre-built KieContainer
     * @param facts List of facts to insert into the session
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return DRLRunnerResult containing execution results
     */
    public DRLRunnerResult executeWithContainer(KieContainer kieContainer, List<Object> facts, int maxRuns) {
        KieSession kieSession = kieContainer.newKieSession();
        
        try {
            insertFacts(kieSession, facts);
            int firedRules = fireRules(kieSession, maxRuns);
            List<Object> resultFacts = collectFacts(kieSession);
            
            logExecutionResults(firedRules, resultFacts);
            
            return new DRLRunnerResult(resultFacts, firedRules);
            
        } finally {
            kieSession.dispose();
        }
    }

    /**
     * Builds a KieContainer from DRL content
     * @param drlContent The DRL content to compile
     * @return Compiled KieContainer
     * @throws RuntimeException if compilation fails
     */
    public KieContainer buildKieContainer(String drlContent) {
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drlContent, ResourceType.DRL);

        if (kieHelper.verify().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("DRL compilation errors: " +
                    kieHelper.verify().getMessages(Message.Level.ERROR));
        }

        return kieHelper.getKieContainer();
    }

    /**
     * Inserts facts into the KieSession
     * @param session The KieSession to insert facts into
     * @param facts List of facts to insert
     */
    private void insertFacts(KieSession session, List<Object> facts) {
        for (Object fact : facts) {
            session.insert(fact);
            System.out.println("Inserted fact: " + fact);
        }
    }

    /**
     * Fires rules in the KieSession
     * @param session The KieSession to fire rules in
     * @param maxRuns Maximum number of rules to fire (0 for unlimited)
     * @return Number of rules fired
     */
    private int fireRules(KieSession session, int maxRuns) {
        int firedRules;
        if (maxRuns > 0) {
            firedRules = session.fireAllRules(maxRuns);
        } else {
            firedRules = session.fireAllRules();
        }
        return firedRules;
    }

    /**
     * Collects all facts from the KieSession working memory
     * @param session The KieSession to collect facts from
     * @return List of facts in working memory
     */
    private List<Object> collectFacts(KieSession session) {
        Collection<?> facts = session.getObjects();
        return new ArrayList<>(facts);
    }

    /**
     * Logs execution results for debugging
     * @param firedRules Number of rules that fired
     * @param facts List of facts in working memory
     */
    private void logExecutionResults(int firedRules, List<Object> facts) {
        System.out.println("Fired " + firedRules + " rules");
        System.out.println("Facts in working memory: " + facts.size());
        facts.forEach(fact -> System.out.println("  " + fact));
    }

    /**
     * Validates input parameters
     * @param drlContent DRL content to validate
     * @param maxRuns Maximum runs parameter to validate
     * @throws IllegalArgumentException if input is invalid
     */
    private void validateInput(String drlContent, int maxRuns) {
        if (drlContent == null || drlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("DRL content cannot be null or empty");
        }
        if (maxRuns < 0) {
            throw new IllegalArgumentException("Maximum runs cannot be negative");
        }
    }
}