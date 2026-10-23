package com.gooci.console.common;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/**
 * @ClassName ExtTargetSupport
 * @Description 外接模块目标：回环地址、探活、网关客户端。探活用 TCP，避免 Vite 编译拖死 HTTP GET
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

    public static HttpClient httpClient(Duration connectTimeout) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public static URI loopback4(URI uri) {
        if (uri == null || uri.getHost() == null) {
            return uri;
        }
        String host = uri.getHost();
        if (!"localhost".equalsIgnoreCase(host) && !"::1".equals(host) && !"[::1]".equals(host)) {
            return uri;
        }
        try {
            return new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    "127.0.0.1",
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment());
        } catch (URISyntaxException ex) {
            return uri;
        }
    }

    public static boolean skipRequestHeader(String name) {
        return name != null && SKIP_REQUEST_HEADERS.contains(name.toLowerCase(Locale.ROOT));
    }

    public static Probe probe(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return new Probe("none", "未填写内网目标，网关无处转发");
        }
        URI uri;
        try {
            uri = loopback4(URI.create(targetUrl.trim()));
        } catch (IllegalArgumentException ex) {
            return new Probe("down", "目标地址不是合法 URL");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return new Probe("down", "目标地址缺少主机");
        }
        int port = uri.getPort();
        if (port < 0) {
            port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress() && address.getAddress().length != 4) {
                address = InetAddress.getByName("127.0.0.1");
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(address, port), 1500);
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
}
