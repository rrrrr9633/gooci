package com.gooci.console;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConsoleApplicationTest {
    @Test
    void loadsRepoRootEnvWithoutOverridingProcessEnvironment() throws Exception {
        Path dir = Files.createTempDirectory("gooci-env");
        Files.writeString(dir.resolve(".env"), "GOOCI_DELIVERY_EDITION=cloud\n# comment\nEMPTY=\nQUOTED='cloud'\n");
        String previous = System.getProperty("user.dir");
        String edition = System.getProperty("GOOCI_DELIVERY_EDITION");
        try {
            System.clearProperty("GOOCI_DELIVERY_EDITION");
            System.setProperty("user.dir", dir.toString());
            ConsoleApplication.loadDotEnv();
            assertEquals("cloud", System.getProperty("GOOCI_DELIVERY_EDITION"));
            assertNull(System.getProperty("EMPTY"));
        } finally {
            if (edition == null) System.clearProperty("GOOCI_DELIVERY_EDITION");
            else System.setProperty("GOOCI_DELIVERY_EDITION", edition);
            System.setProperty("user.dir", previous);
        }
        assertNull(System.getenv("GOOCI_DELIVERY_EDITION"));
    }
}
