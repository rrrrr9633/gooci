package com.gooci.console.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.server.ResponseStatusException;

class CloudDeploymentRegistryTest {
    @Test
    void persistsOneDeploymentPerCompanyWithoutSharingAnOrigin() {
        var database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .setName(UUID.randomUUID() + ";MODE=MySQL").build();
        try {
            new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")).execute(database);
            var jdbc = new JdbcTemplate(database);
            jdbc.update("INSERT INTO TM_ENTER (ID, ENTER_CODE, ENTER_NAME) VALUES ('a', 'a', 'A'), ('b', 'b', 'B')");
            var registry = new CloudDeploymentRegistry(jdbc, "1.1.1.1,8.8.8.8", "cloud");
            assertNull(registry.find("a"));
            var first = registry.register("a", "https://1.1.1.1:443/");
            assertEquals("https://1.1.1.1", first.publicOrigin());
            assertEquals(first, registry.register("a", "https://1.1.1.1"));
            assertEquals(first, registry.find("a"));
            assertEquals(409, assertThrows(ResponseStatusException.class,
                    () -> registry.register("b", "https://1.1.1.1")).getStatusCode().value());
            assertNull(registry.find("b"));
            var replacement = registry.register("a", "https://8.8.8.8");
            assertNotEquals(first.id(), replacement.id());
            assertEquals(replacement, registry.find("a"));
            assertEquals(404, assertThrows(ResponseStatusException.class,
                    () -> registry.register("missing", "https://1.1.1.1")).getStatusCode().value());
            assertEquals(404, assertThrows(ResponseStatusException.class,
                    () -> new CloudDeploymentRegistry(jdbc, "1.1.1.1", "local").register("a", "https://1.1.1.1")).getStatusCode().value());
            assertEquals(replacement, new CloudDeploymentRegistry(jdbc, "1.1.1.1", "local").find("a"));
        } finally {
            database.shutdown();
        }
    }

    @Test
    void intersectsCompanyOpeningAndAccountGrantForTheBoundDeployment() {
        var database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .setName(UUID.randomUUID() + ";MODE=MySQL").build();
        try {
            new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")).execute(database);
            var jdbc = new JdbcTemplate(database);
            jdbc.update("INSERT INTO TM_ENTER (ID, ENTER_CODE, ENTER_NAME) VALUES ('a', 'a', 'A'), ('b', 'b', 'B')");
            jdbc.update("INSERT INTO TM_SYS_USER (ID, LOGIN_NAME, LOGIN_PASSWORD, DISPLAY_NAME, TS_ENTER_ID) VALUES "
                    + "('u1','u1','unused','U1','a'), ('u2','u2','unused','U2','a')");
            jdbc.update("INSERT INTO TM_APP_OPEN (ID, APP_CODE, APP_NAME, TS_ENTER_ID) VALUES "
                    + "('o1','APP_EQKB','设备知识库','a'), ('o2','APP_APS','排程预测','a')");
            jdbc.update("INSERT INTO TR_USER_APP_GRANT (ID, TM_SYS_USER_ID, APP_CODE, APP_NAME, TS_ENTER_ID) VALUES "
                    + "('g1','u1','APP_EQKB','设备知识库','a'), ('g2','u2','APP_APS','排程预测','a'), "
                    + "('g3','u1','APP_RECON','自动对账','a')");
            var registry = new CloudDeploymentRegistry(jdbc, "1.1.1.1,8.8.8.8", "cloud");
            var a = registry.register("a", "https://1.1.1.1");
            var b = registry.register("b", "https://8.8.8.8");
            var access = new CloudModuleAccess(jdbc, registry);
            assertEquals(java.util.List.of("APP_EQKB"), access.accountModules("u1", a.id()));
            assertEquals(java.util.List.of("APP_APS"), access.accountModules("u2", a.id()));
            assertTrue(access.accountModules("u1", b.id()).isEmpty());
            verifyLaunchCodes(jdbc, registry, access, a, b);
            jdbc.update("UPDATE TM_APP_OPEN SET HIDE_FLAG = '1' WHERE ID = 'o1'");
            assertTrue(access.accountModules("u1", a.id()).isEmpty());
        } finally {
            database.shutdown();
        }
    }

    private void verifyLaunchCodes(JdbcTemplate jdbc, CloudDeploymentRegistry registry, CloudModuleAccess access,
            CloudDeploymentRegistry.Deployment a, CloudDeploymentRegistry.Deployment b) {
        var env = new org.springframework.mock.env.MockEnvironment().withProperty("GOOCI_CLOUD_DEPLOYMENT_CREDENTIAL_HASHES",
                "{\"" + a.id() + "\":\"" + digest("credential-a") + "\",\"" + b.id() + "\":\"" + digest("credential-b") + "\"}");
        var launch = new CloudLaunchService(jdbc, registry, access, new com.fasterxml.jackson.databind.ObjectMapper(), env);
        com.gooci.console.security.LoginUserHolder.setUserId("u1");
        try {
            assertThrows(ResponseStatusException.class, () -> launch.issue("APP_APS"));
            var ticket = launch.issue("APP_EQKB");
            assertEquals(a.publicOrigin(), ticket.publicOrigin());
            assertThrows(ResponseStatusException.class, () -> launch.redeem(a.id(), null, ticket.code()));
            assertThrows(ResponseStatusException.class, () -> launch.redeem(b.id(), "credential-b", ticket.code()));
            assertEquals(java.util.List.of("APP_EQKB"), launch.redeem(a.id(), "credential-a", ticket.code()).modules());
            assertThrows(ResponseStatusException.class, () -> launch.redeem(a.id(), "credential-a", ticket.code()));
            var pending = launch.issue("APP_EQKB");
            jdbc.update("UPDATE TM_APP_OPEN SET HIDE_FLAG = '1' WHERE ID = 'o1'");
            assertThrows(ResponseStatusException.class, () -> launch.redeem(a.id(), "credential-a", pending.code()));
        } finally {
            com.gooci.console.security.LoginUserHolder.clear();
        }
    }

    private String digest(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    void rejectsNonRootOrUnapprovedAddresses() {
        var registry = new CloudDeploymentRegistry(null, "1.1.1.1,192.168.1.117", "cloud");
        for (String address : new String[] {"https://192.168.1.117", "http://1.1.1.1", "https://8.8.8.8",
                "https://1.1.1.1/path", "https://user@1.1.1.1", "https://1.1.1.1?x=1", "https://1.1.1.1#tab"}) {
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> registry.publicOrigin(address), address).getStatusCode().value());
        }
    }

    @Test
    void localIssueOpensLoopbackHarnessWithoutCloudRegistration() {
        var database = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .setName(UUID.randomUUID() + ";MODE=MySQL").build();
        try {
            new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")).execute(database);
            var jdbc = new JdbcTemplate(database);
            jdbc.update("INSERT INTO TM_ENTER (ID, ENTER_CODE, ENTER_NAME) VALUES ('a', 'a', 'A')");
            jdbc.update("INSERT INTO TM_SYS_USER (ID, LOGIN_NAME, LOGIN_PASSWORD, DISPLAY_NAME, TS_ENTER_ID) VALUES "
                    + "('u1','u1','unused','U1','a')");
            jdbc.update("INSERT INTO TM_APP_OPEN (ID, APP_CODE, APP_NAME, TS_ENTER_ID) VALUES ('o1','AI_HUB','AI中台','a')");
            jdbc.update("INSERT INTO TR_USER_APP_GRANT (ID, TM_SYS_USER_ID, APP_CODE, APP_NAME, TS_ENTER_ID) VALUES "
                    + "('g1','u1','AI_HUB','AI中台','a')");
            var registry = new CloudDeploymentRegistry(jdbc, "", "local");
            var access = new CloudModuleAccess(jdbc, registry);
            var env = new org.springframework.mock.env.MockEnvironment()
                    .withProperty("GOOCI_LOCAL_DEPLOYMENT_CREDENTIAL", "local-secret");
            var launch = new CloudLaunchService(jdbc, registry, access, new com.fasterxml.jackson.databind.ObjectMapper(), env);
            com.gooci.console.security.LoginUserHolder.setUserId("u1");
            try {
                var ticket = launch.issue("AI_HUB");
                assertEquals("http://127.0.0.1:8888", ticket.publicOrigin());
                assertEquals("AI_HUB", launch.redeem(registry.find("a").id(), "local-secret", ticket.code()).appCode());
            } finally {
                com.gooci.console.security.LoginUserHolder.clear();
            }
            var missing = new CloudLaunchService(jdbc, registry, access, new com.fasterxml.jackson.databind.ObjectMapper(),
                    new org.springframework.mock.env.MockEnvironment());
            com.gooci.console.security.LoginUserHolder.setUserId("u1");
            try {
                assertEquals(403, assertThrows(ResponseStatusException.class, () -> missing.issue("AI_HUB")).getStatusCode().value());
            } finally {
                com.gooci.console.security.LoginUserHolder.clear();
            }
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> com.gooci.console.common.LocalInstallOrigin.normalize("http://120.0.0.1:8888")).getStatusCode().value());
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> com.gooci.console.common.LocalInstallOrigin.normalize("http://192.168.1.10:8888")).getStatusCode().value());
            assertEquals("https://192.168.1.10", com.gooci.console.common.LocalInstallOrigin.normalize("https://192.168.1.10"));
        } finally {
            database.shutdown();
        }
    }
}
