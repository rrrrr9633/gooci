package com.gooci.console.common;

import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Local and LAN employee origins. 120.0.0.1 is not loopback. */
public final class LocalInstallOrigin {
    public static final int PORT = 8888;
    public static final String DEFAULT = "http://127.0.0.1:8888";
    public static final String DEFAULT_DEPLOYMENT_ID = "11111111-1111-4111-8111-111111111111";

    private LocalInstallOrigin() {
    }

    public static String normalize(String address) {
        URI uri;
        try {
            uri = URI.create(address == null ? "" : address.trim());
        } catch (IllegalArgumentException ex) {
            throw invalid();
        }
        if (uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null
                || !(uri.getRawPath() == null || uri.getRawPath().isEmpty() || "/".equals(uri.getRawPath()))) {
            throw invalid();
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (ExtTargetSupport.loopbackHost(host)) {
            if (!"http".equalsIgnoreCase(uri.getScheme()) || uri.getPort() != PORT) throw invalid();
            return "http://" + host + ":" + PORT;
        }
        if (!privateIpv4(host) || !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getPort() == 0 || uri.getPort() > 65535) {
            throw invalid();
        }
        String origin = "https://" + host + (uri.getPort() < 0 || uri.getPort() == 443 ? "" : ":" + uri.getPort());
        if (origin.length() > 255) throw invalid();
        return origin;
    }

    static boolean privateIpv4(String host) {
        String[] parts = host.split("\\.");
        if (parts.length != 4) return false;
        int[] oct = new int[4];
        try {
            for (int i = 0; i < 4; i++) {
                if (parts[i].length() > 1 && parts[i].startsWith("0")) return false;
                oct[i] = Integer.parseInt(parts[i]);
                if (oct[i] < 0 || oct[i] > 255) return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return oct[0] == 10
                || (oct[0] == 172 && oct[1] >= 16 && oct[1] <= 31)
                || (oct[0] == 192 && oct[1] == 168);
    }

    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "本地入口必须是 http://127.0.0.1:8888，局域网必须是 HTTPS");
    }
}
