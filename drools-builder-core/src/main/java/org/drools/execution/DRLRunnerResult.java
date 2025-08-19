package org.drools.execution;

import java.util.List;

public record DRLRunnerResult(List<Object> objects, int firedRules) {
}
