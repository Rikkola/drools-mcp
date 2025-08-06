package org.drools.agentic.example.storage;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Shared storage for knowledge base and session management.
 * Thread-safe singleton that stores the current knowledge base and session.
 */
public class KnowledgeBaseStorage {
    
    private static final KnowledgeBaseStorage INSTANCE = new KnowledgeBaseStorage();
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private KieContainer currentKnowledgeBase;
    private KieSession currentSession;
    private String knowledgeBaseName;
    private LocalDateTime createdTime;
    private String sourceInfo;
    
    private KnowledgeBaseStorage() {
        // Private constructor for singleton
    }
    
    public static KnowledgeBaseStorage getInstance() {
        return INSTANCE;
    }
    
    /**
     * Store a new knowledge base and session (replaces existing)
     */
    public void store(String name, KieContainer kieContainer, KieSession kieSession, String source) {
        lock.writeLock().lock();
        try {
            // Dispose existing session if any
            if (currentSession != null) {
                currentSession.dispose();
            }
            
            // Store new knowledge base and session
            this.knowledgeBaseName = name;
            this.currentKnowledgeBase = kieContainer;
            this.currentSession = kieSession;
            this.createdTime = LocalDateTime.now();
            this.sourceInfo = source;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get the current knowledge base (read-only)
     */
    public KieContainer getKnowledgeBase() {
        lock.readLock().lock();
        try {
            return currentKnowledgeBase;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get the current session (read-only)
     */
    public KieSession getSession() {
        lock.readLock().lock();
        try {
            return currentSession;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get knowledge base metadata
     */
    public KnowledgeBaseInfo getInfo() {
        lock.readLock().lock();
        try {
            if (currentKnowledgeBase == null) {
                return null;
            }
            
            return new KnowledgeBaseInfo(
                knowledgeBaseName,
                currentKnowledgeBase.getReleaseId().toString(),
                currentSession != null ? currentSession.getId() : -1,
                currentSession != null ? currentSession.getFactCount() : 0,
                createdTime,
                sourceInfo,
                currentSession != null
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check if a knowledge base is available
     */
    public boolean hasKnowledgeBase() {
        lock.readLock().lock();
        try {
            return currentKnowledgeBase != null;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check if a session is available
     */
    public boolean hasSession() {
        lock.readLock().lock();
        try {
            return currentSession != null;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Clear all facts from the current session
     */
    public long clearFacts() {
        lock.writeLock().lock();
        try {
            if (currentSession == null) {
                return 0;
            }
            
            long factCount = currentSession.getFactCount();
            currentSession.getFactHandles().forEach(currentSession::delete);
            return factCount;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Dispose the current knowledge base and session
     */
    public void dispose() {
        lock.writeLock().lock();
        try {
            if (currentSession != null) {
                currentSession.dispose();
                currentSession = null;
            }
            
            currentKnowledgeBase = null;
            knowledgeBaseName = null;
            createdTime = null;
            sourceInfo = null;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Knowledge base information record
     */
    public record KnowledgeBaseInfo(
        String name,
        String releaseId,
        long sessionId,
        long factCount,
        LocalDateTime createdTime,
        String sourceInfo,
        boolean sessionActive
    ) {}
}