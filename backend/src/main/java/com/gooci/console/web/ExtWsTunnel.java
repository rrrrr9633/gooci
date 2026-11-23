package com.gooci.console.web;

import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.WebConnection;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @ClassName ExtWsTunnel
 * @Description 门口把浏览器的 WebSocket 原样转到 deepseek-harness，不解释帧
 * @author GooCi
 * @date 2026-03-26
 */
public final class ExtWsTunnel implements HttpUpgradeHandler {

    private static final ThreadLocal<Socket> PENDING = new ThreadLocal<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private Socket upstream;
    private WebConnection browser;

    static void arm(Socket upstream) {
        PENDING.set(upstream);
    }

    static void clear() {
        PENDING.remove();
    }

    static Socket open(String scheme, String host, int port) throws IOException {
        int actual = port > 0 ? port : ("https".equalsIgnoreCase(scheme) ? 443 : 80);
        if ("https".equalsIgnoreCase(scheme)) {
            return SSLSocketFactory.getDefault().createSocket(host, actual);
        }
        return new Socket(host, actual);
    }

    static final class Handshake {
        final Socket socket;
        final String head;

        Handshake(Socket socket, String head) {
            this.socket = socket;
            this.head = head;
        }
    }

    /**
     * 用浏览器原来的 Key 向 harness 升级。套接字上的 101 头已被读走。
     */
    static Handshake handshake(String scheme, String host, int port, String requestHead) throws IOException {
        Socket socket = open(scheme, host, port);
        socket.setTcpNoDelay(true);
        OutputStream out = socket.getOutputStream();
        out.write(requestHead.getBytes(StandardCharsets.ISO_8859_1));
        out.flush();
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream head = new ByteArrayOutputStream();
        int previous = -1;
        int matches = 0;
        while (head.size() < 65536) {
            int next = in.read();
            if (next < 0) {
                socket.close();
                throw new IOException("harness 在升级完成前关闭");
            }
            head.write(next);
            if (previous == '\r' && next == '\n') {
                matches++;
            } else if (next != '\r') {
                matches = 0;
            }
            previous = next;
            if (matches >= 2) {
                break;
            }
        }
        String status = head.toString(StandardCharsets.ISO_8859_1);
        int line = status.indexOf("\r\n");
        String first = line < 0 ? status : status.substring(0, line);
        if (!first.contains(" 101 ")) {
            socket.close();
            throw new IOException(first.isBlank() ? "harness 拒绝升级" : first);
        }
        return new Handshake(socket, status);
    }

    @Override
    public void init(WebConnection connection) {
        this.browser = connection;
        this.upstream = PENDING.get();
        clear();
        if (upstream == null || browser == null) {
            destroy();
            return;
        }
        Thread down = new Thread(() -> pump(upstream, browser, true), "ext-ws-down");
        Thread up = new Thread(() -> pump(browser, upstream, false), "ext-ws-up");
        down.setDaemon(true);
        up.setDaemon(true);
        down.start();
        up.start();
    }

    private void pump(Object from, Object to, boolean fromSocket) {
        try {
            InputStream in = fromSocket ? ((Socket) from).getInputStream() : ((WebConnection) from).getInputStream();
            OutputStream out = fromSocket ? ((WebConnection) to).getOutputStream() : ((Socket) to).getOutputStream();
            in.transferTo(out);
            out.flush();
        } catch (IOException ignored) {
            // 任一侧关闭即拆隧道
        } finally {
            destroy();
        }
    }

    @Override
    public void destroy() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        if (upstream != null) {
            try {
                upstream.close();
            } catch (IOException ignored) {
                // 关闭失败不影响浏览器侧释放
            }
        }
        if (browser != null) {
            try {
                browser.close();
            } catch (Exception ignored) {
                // 容器可能已经拆掉升级连接
            }
        }
    }

    static boolean websocket(String upgrade) {
        return upgrade != null && upgrade.toLowerCase(Locale.ROOT).contains("websocket");
    }
}
