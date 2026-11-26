package com.gooci.console.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gooci.console.security.LoginUserHolder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CloudLaunchService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final CloudDeploymentRegistry registry;
    private final CloudModuleAccess access;
    private final Map<String, String> credentialHashes;
    private final String localCredential;
    private final Clock clock = Clock.systemUTC();

    public CloudLaunchService(JdbcTemplate jdbc, CloudDeploymentRegistry registry, CloudModuleAccess access,
            ObjectMapper mapper, Environment environment) {
        String configured = environment.getProperty("GOOCI_CLOUD_DEPLOYMENT_CREDENTIAL_HASHES", "{}");
        this.jdbc = jdbc;
        this.registry = registry;
        this.access = access;
        this.localCredential = environment.getProperty("GOOCI_LOCAL_DEPLOYMENT_CREDENTIAL", "");
        try {
            credentialHashes = configured.isBlank() ? Map.of()
                    : Map.copyOf(mapper.readValue(configured, new TypeReference<Map<String, String>>() {}));
            if (credentialHashes.values().stream().anyMatch(hash -> !hash.matches("[0-9a-f]{64}"))
                    || credentialHashes.values().stream().distinct().count() != credentialHashes.size()) {
                throw new IllegalArgumentException();
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid GOOCI_CLOUD_DEPLOYMENT_CREDENTIAL_HASHES");
        }
    }

    public record Launch(String publicOrigin, String code) {}
    public record Identity(String userId, String appCode, List<String> modules) {}
    private record Ticket(String userId, String appCode, long expiresAt) {}

    @Transactional
    public Launch issue(String appCode) {
        if (!registry.available() && !registry.local()) throw missing();
        String userId = LoginUserHolder.getUserId();
        if (LoginUserHolder.ops() || userId == null || userId.isBlank()) throw forbidden();
        var enterprise = jdbc.query("SELECT TS_ENTER_ID FROM TM_SYS_USER WHERE ID = ? AND IS_DELETE = '0'",
                (rs, row) -> rs.getString(1), userId);
        if (enterprise.isEmpty() || enterprise.getFirst() == null) throw forbidden();
        var deployment = registry.available() ? registry.find(enterprise.getFirst())
                : registry.local() ? registry.ensureLocal(enterprise.getFirst()) : null;
        if (deployment == null
                || (registry.available() ? !credentialHashes.containsKey(deployment.id())
                        : !registry.local() || localCredential.isBlank())
                || !access.currentAccountModules(deployment.id()).contains(appCode)) throw forbidden();
        String origin = registry.available() ? registry.publicOrigin(deployment.publicOrigin()) : deployment.publicOrigin();
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        long now = clock.millis();
        jdbc.update("DELETE FROM TR_CLOUD_LAUNCH WHERE EXPIRES_AT <= ?", now);
        jdbc.update("INSERT INTO TR_CLOUD_LAUNCH (CODE_HASH, TM_SYS_USER_ID, DEPLOYMENT_ID, APP_CODE, EXPIRES_AT) VALUES (?, ?, ?, ?, ?)",
                hash(code), userId, deployment.id(), appCode, now + 60_000);
        return new Launch(origin, code);
    }

    @Transactional
    public Identity redeem(String deploymentId, String credential, String code) {
        if (!registry.available() && !registry.local()) throw missing();
        String expected = registry.available() ? credentialHashes.get(deploymentId)
                : registry.local() && !localCredential.isBlank() ? hash(localCredential) : null;
        if (expected == null || credential == null || credential.length() > 256
                || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                        hash(credential).getBytes(StandardCharsets.US_ASCII))
                || code == null || !code.matches("[A-Za-z0-9_-]{43}")) throw forbidden();
        String digest = hash(code);
        var tickets = jdbc.query("""
                SELECT TM_SYS_USER_ID, APP_CODE, EXPIRES_AT FROM TR_CLOUD_LAUNCH
                WHERE CODE_HASH = ? AND DEPLOYMENT_ID = ? FOR UPDATE
                """, (rs, row) -> new Ticket(rs.getString(1), rs.getString(2), rs.getLong(3)), digest, deploymentId);
        if (tickets.isEmpty()) throw forbidden();
        var ticket = tickets.getFirst();
        if (ticket.expiresAt() <= clock.millis()) throw forbidden();
        var modules = access.accountModules(ticket.userId(), deploymentId);
        if (!modules.contains(ticket.appCode())) throw forbidden();
        if (jdbc.update("DELETE FROM TR_CLOUD_LAUNCH WHERE CODE_HASH = ?", digest) != 1) throw forbidden();
        return new Identity(ticket.userId(), ticket.appCode(), modules);
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static ResponseStatusException forbidden() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private static ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
