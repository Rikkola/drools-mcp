package org.drools.validation;

import org.drools.io.ClassPathResource;
import org.drools.io.InputStreamResource;
import org.drools.verifier.EmptyVerifierConfiguration;
import org.drools.verifier.Verifier;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.drools.verifier.report.components.Severity;
import org.drools.verifier.report.components.VerifierMessageBase;
import org.kie.api.io.ResourceType;

import java.io.ByteArrayInputStream;
import java.util.Collection;

public class DRLVerifier {

    public String verify(String code) {

        final VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();

        final EmptyVerifierConfiguration verifierConfiguration = new EmptyVerifierConfiguration();
        verifierConfiguration.getVerifyingResources().put(
                new ClassPathResource( "MyValidation.drl",
                        DRLVerifier.class ),
                ResourceType.DRL

        );

        final Verifier verifier = vBuilder.newVerifier(verifierConfiguration);
        verifier.addResourcesToVerify(
                new InputStreamResource(new ByteArrayInputStream(code.getBytes())),
                ResourceType.DRL);

        verifier.fireAnalysis();

        final StringBuilder result = new StringBuilder();
        boolean hasIssues = false;

        // First check for compilation/parsing errors (most critical)
        if (verifier.hasErrors()) {
            hasIssues = true;
            for (org.drools.verifier.VerifierError error : verifier.getErrors()) {
                result.append("ERROR: ").append(error.getMessage()).append("\n");
            }
        }

        // Check for messages of all severity levels (ERROR, WARNING, NOTE)
        final Collection<VerifierMessageBase> errorMessages = verifier.getResult().getBySeverity(Severity.ERROR);
        final Collection<VerifierMessageBase> warningMessages = verifier.getResult().getBySeverity(Severity.WARNING);
        final Collection<VerifierMessageBase> noteMessages = verifier.getResult().getBySeverity(Severity.NOTE);

        // Add error messages (highest priority)
        if (!errorMessages.isEmpty()) {
            hasIssues = true;
            for (VerifierMessageBase message : errorMessages) {
                result.append("ERROR: ").append(message.getMessage()).append("\n");
            }
        }

        // Add warning messages
        if (!warningMessages.isEmpty()) {
            hasIssues = true;
            for (VerifierMessageBase message : warningMessages) {
                result.append("WARNING: ").append(message.getMessage()).append("\n");
            }
        }

        // Add note messages
        if (!noteMessages.isEmpty()) {
            hasIssues = true;
            for (VerifierMessageBase message : noteMessages) {
                result.append("NOTE: ").append(message.getMessage()).append("\n");
            }
        }

        if (hasIssues) {
            return result.toString().trim();
        } else {
            return "Code looks good";
        }
    }
}