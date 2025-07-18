package org.drools.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DRLVerifierTest {

    @Test
    public void testValidDrl() {
        DRLVerifier verifier = new DRLVerifier();
        String validDrlContent = "package org.drools;\n" +
                                "rule \"Example Rule\"\n" +
                                "when\n" +
                                "    $fact : Object()\n" +
                                "then\n" +
                                "    System.out.println(\"This is an example rule\");\n" +
                                "end";

        String result = verifier.verify(validDrlContent);
        assertEquals("Code looks good", result);
    }

    @Test
    public void testInvalidDrl() {
        DRLVerifier verifier = new DRLVerifier();

        String invalidDrlContent =
                "package org.drools;\n" +
                "rule \"example Rule\"\n" +
                "when\n" +
                "    $fact : Object()\n" +
                "then\n" +
                "    System.out.println(\"This is an example rule\");\n" +
                "end";

        String result = verifier.verify(invalidDrlContent);
        assertTrue(result.contains("The rule name 'example Rule' needs to start with a capital letter."), "Result should contain error information");
    }

}
