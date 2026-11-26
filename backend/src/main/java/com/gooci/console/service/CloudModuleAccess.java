package com.gooci.console.service;

import com.gooci.console.common.AiFamily;
import com.gooci.console.security.LoginUserHolder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Loads the company and account permissions for a registered deployment. */
@Service
public class CloudModuleAccess {
    private final JdbcTemplate jdbc;
    private final CloudDeploymentRegistry registry;
    private final Set<String> localModules;

    public CloudModuleAccess(JdbcTemplate jdbc, CloudDeploymentRegistry registry) {
        this(jdbc, registry, "");
    }

    @Autowired
    public CloudModuleAccess(JdbcTemplate jdbc, CloudDeploymentRegistry registry,
            @Value("${GOOCI_LOCAL_MODULES:}") String localModules) {
        this.jdbc = jdbc;
        this.registry = registry;
        this.localModules = Set.copyOf(Arrays.stream((localModules == null ? "" : localModules).split(","))
                .map(String::trim).filter(code -> AiFamily.HUB.equals(code) || AiFamily.function(code)).toList());
    }

    public List<String> currentAccountModules(String deploymentId) {
        String userId = LoginUserHolder.getUserId();
        if (LoginUserHolder.ops() || userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return accountModules(userId, deploymentId);
    }

    // Callers must derive userId from an authenticated identity, never a request field.
    List<String> accountModules(String userId, String deploymentId) {
        return jdbc.queryForList("""
                SELECT DISTINCT o.APP_CODE FROM TM_SYS_USER u
                JOIN TM_ENTER e ON e.ID = u.TS_ENTER_ID AND e.IS_DELETE = '0'
                JOIN TM_CLOUD_DEPLOYMENT d ON d.TS_ENTER_ID = e.ID
                JOIN TM_APP_OPEN o ON o.TS_ENTER_ID = e.ID AND o.IS_DELETE = '0' AND o.HIDE_FLAG = '0'
                JOIN TR_USER_APP_GRANT g ON g.TS_ENTER_ID = e.ID AND g.TM_SYS_USER_ID = u.ID
                    AND g.APP_CODE = o.APP_CODE AND g.IS_DELETE = '0'
                WHERE u.ID = ? AND u.IS_DELETE = '0' AND d.DEPLOYMENT_ID = ?
                ORDER BY o.APP_CODE
                """, String.class, userId, deploymentId).stream()
                .filter(code -> AiFamily.HUB.equals(code) || AiFamily.function(code))
                .filter(code -> !registry.local() || localModules.isEmpty() || localModules.contains(code))
                .toList();
    }
}
