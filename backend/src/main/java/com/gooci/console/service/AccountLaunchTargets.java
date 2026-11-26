package com.gooci.console.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooci.console.common.AiFamily;
import com.gooci.console.common.ExtTargetSupport;
import com.gooci.console.security.LoginUserHolder;
import java.net.URI;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

/** Resolves browser destinations from operator configuration, keyed by authenticated user ID. */
@Service
public class AccountLaunchTargets {
    private final Map<String, Map<String, String>> accounts;
    private final ExtTargetSupport.Policy policy;
    private final boolean local;
    private final boolean localDelivery;

    public AccountLaunchTargets(ObjectMapper mapper, Environment environment,
            @Value("${gooci.ext-allow-hosts:}") String allowHosts) {
        String configured = environment.getProperty("GOOCI_ACCOUNT_LAUNCH_TARGETS", "{}");
        local = environment.acceptsProfiles(Profiles.of("local & !prod"));
        localDelivery = "local".equals(environment.getProperty("GOOCI_DELIVERY_EDITION"));
        policy = ExtTargetSupport.policy(allowHosts, true);
        try {
            accounts = mapper.readValue(configured, new TypeReference<>() {});
            if (accounts == null || accounts.values().stream().anyMatch(routes -> routes == null)) {
                throw new IllegalArgumentException();
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("GOOCI_ACCOUNT_LAUNCH_TARGETS must map user IDs to module URL maps");
        }
    }

    public String resolve(String appCode, String enterpriseTarget) {
        String userId = LoginUserHolder.getUserId();
        if (localDelivery || userId == null || userId.isBlank() || LoginUserHolder.ops()) return "";
        if (AiFamily.HUB.equals(appCode) || AiFamily.function(appCode)) return "";
        var routes = accounts.getOrDefault(userId, Map.of());
        String candidate = routes.containsKey(appCode) ? routes.get(appCode) : enterpriseTarget;
        if (candidate == null || candidate.isBlank()) return "";
        String target = candidate.trim();
        try {
            URI uri = URI.create(target);
            if (uri.getRawUserInfo() != null) return "";
            String host = uri.getHost();
            boolean devLoopback = local && ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host));
            return ExtTargetSupport.denyReason(target, policy, devLoopback) == null ? target : "";
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }
}
