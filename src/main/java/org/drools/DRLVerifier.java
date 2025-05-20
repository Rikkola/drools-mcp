package org.drools;

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

        final Collection<VerifierMessageBase> messages = verifier.getResult().getBySeverity(Severity.NOTE);

        if (!messages.isEmpty()) {
            final StringBuilder result = new StringBuilder();
            for (VerifierMessageBase message : messages) {
                result.append(message.getMessage());
            }
            return result.toString();
        } else {
            return "Code looks good";
        }
    }
}
