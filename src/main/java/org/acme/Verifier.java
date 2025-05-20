package org.acme;

import org.drools.io.InputStreamResource;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.kie.api.io.ResourceType;

import java.io.ByteArrayInputStream;

public class Verifier {

    public String verify(String code) {

        final VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();

        vBuilder.newVerifier().addResourcesToVerify(new InputStreamResource(new ByteArrayInputStream(code.getBytes())), ResourceType.DRL);

        return "Code looks good";
    }
}
