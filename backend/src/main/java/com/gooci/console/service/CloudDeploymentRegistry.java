package com.gooci.console.service;

import com.gooci.console.common.ExtTargetSupport;
import com.gooci.console.common.LocalInstallOrigin;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CloudDeploymentRegistry {
    private final JdbcTemplate jdbc;
    private final ExtTargetSupport.Policy policy;
    private final boolean cloud;
    private final boolean local;
    private final String localOrigin;
    private final String localDeploymentId;

    public CloudDeploymentRegistry(JdbcTemplate jdbc, String allowHosts, String edition) {
        this(jdbc, allowHosts, edition, LocalInstallOrigin.DEFAULT, LocalInstallOrigin.DEFAULT_DEPLOYMENT_ID);
    }

    @Autowired
    public CloudDeploymentRegistry(JdbcTemplate jdbc,
            @Value("${gooci.ext-allow-hosts:}") String allowHosts,
            @Value("${GOOCI_DELIVERY_EDITION:}") String edition,
            @Value("${GOOCI_LOCAL_HARNESS_ORIGIN:" + LocalInstallOrigin.DEFAULT + "}") String localOrigin,
            @Value("${GOOCI_LOCAL_DEPLOYMENT_ID:" + LocalInstallOrigin.DEFAULT_DEPLOYMENT_ID + "}") String localDeploymentId) {
        this.jdbc = jdbc;
        this.policy = ExtTargetSupport.policy(allowHosts, true);
        String kind = edition == null ? "" : edition.trim();
        this.cloud = "cloud".equals(kind);
        this.local = "local".equals(kind);
        this.localOrigin = local ? LocalInstallOrigin.normalize(localOrigin) : LocalInstallOrigin.DEFAULT;
        this.localDeploymentId = localDeploymentId == null || localDeploymentId.isBlank()
                ? LocalInstallOrigin.DEFAULT_DEPLOYMENT_ID : localDeploymentId.trim();
        if (!this.localDeploymentId.matches("[0-9a-fA-F-]{36}")) {
            throw new IllegalArgumentException("Invalid GOOCI_LOCAL_DEPLOYMENT_ID");
        }
    }

    public record Deployment(String id, String publicOrigin) {}

    public Deployment find(String enterId) {
        return jdbc.query("""
                SELECT d.DEPLOYMENT_ID, d.PUBLIC_ORIGIN FROM TM_CLOUD_DEPLOYMENT d
                JOIN TM_ENTER e ON e.ID = d.TS_ENTER_ID
                WHERE e.ID = ? AND e.IS_DELETE = '0'
                """, (rs, n) -> new Deployment(rs.getString(1), rs.getString(2)), enterId)
                .stream().findFirst().orElse(null);
    }

    @Transactional
    public Deployment ensureLocal(String enterId) {
        if (!local) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var company = jdbc.query("SELECT ID FROM TM_ENTER WHERE ID = ? AND IS_DELETE = '0' FOR UPDATE",
                (rs, n) -> rs.getString(1), enterId);
        if (company.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "企业不存在");
        Deployment current = find(enterId);
        if (current != null && current.id().equals(localDeploymentId) && current.publicOrigin().equals(localOrigin)) {
            return current;
        }
        try {
            if (current == null) {
                jdbc.update("INSERT INTO TM_CLOUD_DEPLOYMENT (TS_ENTER_ID, DEPLOYMENT_ID, PUBLIC_ORIGIN) VALUES (?, ?, ?)",
                        enterId, localDeploymentId, localOrigin);
            } else {
                jdbc.update("UPDATE TM_CLOUD_DEPLOYMENT SET DEPLOYMENT_ID = ?, PUBLIC_ORIGIN = ? WHERE TS_ENTER_ID = ?",
                        localDeploymentId, localOrigin, enterId);
            }
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "本机入口已登记");
        }
        return new Deployment(localDeploymentId, localOrigin);
    }

    @Transactional
    public Deployment register(String enterId, String address) {
        requireCloud();
        String origin = publicOrigin(address);
        var company = jdbc.query("SELECT ID FROM TM_ENTER WHERE ID = ? AND IS_DELETE = '0' FOR UPDATE",
                (rs, n) -> rs.getString(1), enterId);
        if (company.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "企业不存在");
        Deployment current = find(enterId);
        if (current != null && current.publicOrigin().equals(origin)) return current;
        String id = UUID.randomUUID().toString();
        try {
            if (current == null) {
                jdbc.update("INSERT INTO TM_CLOUD_DEPLOYMENT (TS_ENTER_ID, DEPLOYMENT_ID, PUBLIC_ORIGIN) VALUES (?, ?, ?)",
                        enterId, id, origin);
            } else {
                jdbc.update("UPDATE TM_CLOUD_DEPLOYMENT SET DEPLOYMENT_ID = ?, PUBLIC_ORIGIN = ? WHERE TS_ENTER_ID = ?",
                        id, origin, enterId);
            }
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "公网入口已登记");
        }
        return new Deployment(id, origin);
    }

    String publicOrigin(String address) {
        URI uri;
        try {
            uri = URI.create(address == null ? "" : address.trim());
        } catch (IllegalArgumentException ex) {
            throw invalidOrigin();
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null
                || !(uri.getRawPath().isEmpty() || "/".equals(uri.getRawPath()))
                || uri.getPort() == 0 || uri.getPort() > 65535 || uri.getHost().endsWith(".")) {
            throw invalidOrigin();
        }
        String origin = "https://" + uri.getHost().toLowerCase(Locale.ROOT)
                + (uri.getPort() < 0 || uri.getPort() == 443 ? "" : ":" + uri.getPort());
        if (origin.length() > 255 || ExtTargetSupport.denyReason(origin, policy) != null) throw invalidOrigin();
        return origin;
    }

    public boolean available() {
        return cloud;
    }

    public boolean local() {
        return local;
    }

    private void requireCloud() {
        if (!cloud) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    private static ResponseStatusException invalidOrigin() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "部署入口必须是允许名单内的公网 HTTPS 根地址");
    }
}
