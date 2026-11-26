package com.gooci.console.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooci.console.security.LoginUserHolder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountLaunchTargetsTest {
    @Test
    void localDeliveryDoesNotPublishLegacyAccountOrEnterpriseTargets() {
        String target = "https://1.1.1.1/";
        MockEnvironment env = new MockEnvironment().withProperty(
                "GOOCI_ACCOUNT_LAUNCH_TARGETS", "{\"employee\":{\"AI_HUB\":\"" + target + "\"}}");
        LoginUserHolder.setUserId("employee");
        try {
            AccountLaunchTargets cloud = new AccountLaunchTargets(new ObjectMapper(), env, "1.1.1.1");
            assertEquals("", cloud.resolve("AI_HUB", null));
            assertEquals("", cloud.resolve("APP_EQKB", target));
            assertEquals(target, cloud.resolve("EXT_PDM", target));

            env.withProperty("GOOCI_DELIVERY_EDITION", "local");
            AccountLaunchTargets local = new AccountLaunchTargets(new ObjectMapper(), env, "1.1.1.1");
            assertEquals("", local.resolve("AI_HUB", null));
            assertEquals("", local.resolve("EXT_PDM", target));
        } finally {
            LoginUserHolder.clear();
        }
    }
}
