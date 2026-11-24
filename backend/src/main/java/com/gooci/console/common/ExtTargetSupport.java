package com.gooci.console.common;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @ClassName ExtTargetSupport
 * @Description 外接模块下一跳只允许允许名单内的公网 https。本机回环和内网地址已停用。测试档仅 AI 中台可转发到回环 http(s)
 * @author GooCi
 * @date 2026-03-26
 */
public final class ExtTargetSupport {

    public static final Set<String> SKIP_REQUEST_HEADERS = Set.of(
            "host", "content-length", "connection", "accept-encoding", "cookie", "authorization",
            "origin", "referer", "keep-alive", "te", "trailer", "upgrade", "transfer-encoding",
            "proxy-connection", "expect", "http2-settings");

    private ExtTargetSupport() {
    }

    public record Policy(Set<String> allowHosts, boolean publicHttpsOnly) {
    }

    public static Policy policy(String allowHostsText, boolean publicHttpsOnly) {
        Set<String> hosts = new LinkedHashSet<>();
        if (allowHostsText != null && !allowHostsText.isBlank()) {
            for (String part : allowHostsText.split(",")) {
                String host = part.trim().toLowerCase(Locale.ROOT);
                if (!host.isEmpty()) {
                    hosts.add(host);
                }
            }
        }
        return new Policy(Set.copyOf(hosts), publicHttpsOnly);
    }

    public static HttpClient httpClient(Duration connectTimeout) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public static boolean skipRequestHeader(String name) {
        return name != null && SKIP_REQUEST_HEADERS.contains(name.toLowerCase(Locale.ROOT));
    }

    public static boolean loopbackHost(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        String value = host.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        return "localhost".equals(value) || "127.0.0.1".equals(value) || "::1".equals(value);
    }

    public static boolean retiredLocalHop(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return false;
        }
        URI uri;
        try {
            uri = URI.create(targetUrl.trim());
        } catch (IllegalArgumentException ex) {
            return true;
        }
        String scheme = uri.getScheme();
        if (scheme == null || "http".equalsIgnoreCase(scheme)) {
            return true;
        }
        String host = uri.getHost();
        return host == null || host.isBlank() || localName(host) || privateLiteral(host);
    }

    public static String denyReason(String targetUrl, Policy policy) {
        return denyReason(targetUrl, policy, false);
    }

    public static String denyReason(String targetUrl, Policy policy, boolean hubLocalForward) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return "未填写下一跳，网关无处转发";
        }
        URI uri;
        try {
            uri = URI.create(targetUrl.trim());
        } catch (IllegalArgumentException ex) {
            return "目标地址不是合法 URL";
        }
        Policy gate = policy == null ? ExtTargetSupport.policy("", true) : policy;
        if (!gate.publicHttpsOnly()) {
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && host != null && !host.isBlank()) {
                return null;
            }
        }
        if (hubLocalForward && loopbackUrl(uri)) {
            return null;
        }
        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme)) {
            return "已停用本机连接，下一跳必须是公网 https";
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return "目标地址缺少主机";
        }
        if (localName(host) || privateLiteral(host)) {
            return "已停用本机和内网下一跳，请改到其他云服务器的公网 https";
        }
        String key = host.toLowerCase(Locale.ROOT);
        if (gate.allowHosts().isEmpty() || !gate.allowHosts().contains(key)) {
            return "下一跳不在允许名单";
        }
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses.length == 0) {
                return "主机名无法解析";
            }
            for (InetAddress address : addresses) {
                if (!publicAddress(address)) {
                    return "下一跳解析到非公网地址";
                }
            }
        } catch (UnknownHostException ex) {
            return "主机名无法解析";
        }
        return null;
    }

    public static Probe probe(String targetUrl, Policy policy) {
        return probe(targetUrl, policy, false);
    }

    public static Probe probe(String targetUrl, Policy policy, boolean hubLocalForward) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return new Probe("none", "未填写下一跳，网关无处转发");
        }
        String deny = denyReason(targetUrl, policy, hubLocalForward);
        if (deny != null) {
            return new Probe("down", deny);
        }
        URI uri;
        try {
            uri = URI.create(targetUrl.trim());
        } catch (IllegalArgumentException ex) {
            return new Probe("down", "目标地址不是合法 URL");
        }
        String host = uri.getHost();
        int port = uri.getPort();
        if (port < 0) {
            port = "http".equalsIgnoreCase(uri.getScheme()) ? 80 : 443;
        }
        boolean loopback = hubLocalForward && loopbackUrl(uri);
        try {
            InetAddress address = InetAddress.getByName(host);
            if (!loopback && !publicAddress(address)) {
                return new Probe("down", "下一跳解析到非公网地址");
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(address, port), 3000);
            }
            return new Probe("up", "端口已监听 " + address.getHostAddress() + ":" + port);
        } catch (UnknownHostException ex) {
            return new Probe("down", "主机名无法解析");
        } catch (ConnectException ex) {
            return new Probe("down", "连接被拒绝：该地址端口没有进程在听");
        } catch (Exception ex) {
            String message = ex.getMessage();
            return new Probe("down", message == null || message.isBlank() ? "无法连通模块机" : message);
        }
    }

    public record Probe(String health, String hint) {
    }

    private static boolean loopbackUrl(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return false;
        }
        return loopbackHost(uri.getHost());
    }

    private static boolean localName(String host) {
        if (loopbackHost(host)) {
            return true;
        }
        String value = host.trim().toLowerCase(Locale.ROOT);
        return value.endsWith(".local")
                || value.endsWith(".localhost")
                || value.endsWith(".internal")
                || value.endsWith(".lan");
    }

    private static boolean privateLiteral(String host) {
        String value = host.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        boolean ipv4 = value.chars().allMatch(ch -> (ch >= '0' && ch <= '9') || ch == '.');
        boolean ipv6 = value.indexOf(':') >= 0;
        if (!ipv4 && !ipv6) {
            return false;
        }
        try {
            return !publicAddress(InetAddress.getByName(value));
        } catch (UnknownHostException ex) {
            return true;
        }
    }

    private static boolean publicAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return false;
        }
        byte[] raw = address.getAddress();
        if (raw.length == 16) {
            int head = raw[0] & 0xff;
            if ((head & 0xfe) == 0xfc) {
                return false;
            }
        }
        return true;
    }
}
