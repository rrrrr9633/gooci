package com.gooci.console.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gooci.console.common.ExtTargetSupport;
import com.gooci.console.domain.AppOpen;
import com.gooci.console.mapper.AppOpenMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName ModuleShellController
 * @Description 外接模块同域入口。浏览器只访问 /ext/{appCode}/** ，网关转到 TARGET_URL
 * @author GooCi
 * @date 2026-03-26
 */
@Controller
public class ModuleShellController {

    private static final Set<String> SKIP_OUT = Set.of(
            "transfer-encoding", "connection", "content-length", "x-frame-options", "content-security-policy",
            "content-security-policy-report-only", "cross-origin-opener-policy", "cross-origin-embedder-policy",
            "cross-origin-resource-policy");

    private final AppOpenMapper appOpenMapper;
    private final HttpClient httpClient;

    public ModuleShellController(AppOpenMapper appOpenMapper) {
        this.appOpenMapper = appOpenMapper;
        this.httpClient = ExtTargetSupport.httpClient(Duration.ofSeconds(5));
    }

    @RequestMapping({"/ext/{appCode}", "/ext/{appCode}/", "/ext/{appCode}/**"})
    public void open(@PathVariable String appCode, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        AppOpen open = findOpen(appCode);
        if (open == null) {
            writeHtml(response, HttpServletResponse.SC_NOT_FOUND, missingHtml(appCode));
            return;
        }
        if (open.getTargetUrl() != null && !open.getTargetUrl().isBlank()) {
            String target = joinTarget(open.getTargetUrl().trim(), extraPath(request, appCode), request.getQueryString());
            String error = proxy(open, target, request, response);
            if (error == null) {
                return;
            }
            writeHtml(response, HttpServletResponse.SC_BAD_GATEWAY, downHtml(open, error));
            return;
        }
        writeHtml(response, HttpServletResponse.SC_OK, stubHtml(open));
    }

    private AppOpen findOpen(String appCode) {
        if (appCode == null || appCode.isBlank()) {
            return null;
        }
        List<AppOpen> rows = appOpenMapper.selectList(new LambdaQueryWrapper<AppOpen>()
                .eq(AppOpen::getAppCode, appCode.trim())
                .last("LIMIT 8"));
        for (AppOpen row : rows) {
            if ("1".equals(row.getHideFlag())) {
                continue;
            }
            if (row.getEntryPath() != null && !row.getEntryPath().isBlank()) {
                return row;
            }
        }
        return null;
    }

    private String extraPath(HttpServletRequest request, String appCode) {
        String uri = request.getRequestURI();
        String prefix = request.getContextPath() + "/ext/" + appCode;
        String extra = uri.length() >= prefix.length() ? uri.substring(prefix.length()) : "";
        if (extra.contains("..")) {
            return "/";
        }
        if (extra.isBlank()) {
            return "/";
        }
        return extra.startsWith("/") ? extra : "/" + extra;
    }

    private String joinTarget(String targetUrl, String extra, String query) {
        URI uri;
        try {
            uri = ExtTargetSupport.loopback4(URI.create(targetUrl));
        } catch (IllegalArgumentException ex) {
            return null;
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return null;
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            return null;
        }
        StringBuilder base = new StringBuilder();
        base.append(scheme).append("://").append(uri.getHost());
        if (uri.getPort() > 0) {
            base.append(':').append(uri.getPort());
        }
        String originPath = uri.getPath();
        if (originPath != null && !originPath.isBlank() && !"/".equals(originPath)) {
            if (originPath.endsWith("/")) {
                originPath = originPath.substring(0, originPath.length() - 1);
            }
            base.append(originPath);
        }
        String path = extra == null || extra.isBlank() ? "/" : extra;
        String joined = base.append(path).toString();
        if (query != null && !query.isBlank()) {
            joined = joined + "?" + query;
        }
        return joined;
    }

    private String proxy(AppOpen open, String targetUrl, HttpServletRequest inbound, HttpServletResponse outbound)
            throws IOException {
        if (targetUrl == null || targetUrl.isBlank()) {
            return "目标地址为空";
        }
        URI uri;
        try {
            uri = ExtTargetSupport.loopback4(URI.create(targetUrl));
        } catch (IllegalArgumentException ex) {
            return "目标地址不是合法 URL";
        }
        byte[] body = inbound.getInputStream().readAllBytes();
        String method = inbound.getMethod() == null ? "GET" : inbound.getMethod();
        HttpRequest.BodyPublisher publisher;
        if (body.length == 0 || "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            publisher = HttpRequest.BodyPublishers.noBody();
        } else {
            publisher = HttpRequest.BodyPublishers.ofByteArray(body);
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(60))
                .method(method, publisher);
        Enumeration<String> names = inbound.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            if (name == null || ExtTargetSupport.skipRequestHeader(name)) {
                continue;
            }
            Enumeration<String> values = inbound.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = values.nextElement();
                try {
                    builder.header(name, value);
                } catch (IllegalArgumentException ignored) {
                    // Java HttpClient 拒绝部分 hop-by-hop 头，跳过即可
                }
            }
        }
        try {
            HttpResponse<byte[]> remote = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            outbound.setStatus(remote.statusCode());
            String contentType = remote.headers().firstValue("Content-Type").orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            outbound.setContentType(contentType);
            remote.headers().map().forEach((header, values) -> {
                if (header == null || SKIP_OUT.contains(header.toLowerCase(Locale.ROOT))) {
                    return;
                }
                for (String value : values) {
                    if ("location".equalsIgnoreCase(header)) {
                        outbound.setHeader(header, rewriteLocation(open, targetUrl, value));
                    } else {
                        outbound.addHeader(header, value);
                    }
                }
            });
            byte[] payload = remote.body() == null ? new byte[0] : remote.body();
            payload = maybeRewrite(open.getAppCode(), contentType, payload);
            outbound.getOutputStream().write(payload);
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "转发被中断";
        } catch (IOException | IllegalArgumentException ex) {
            String message = ex.getMessage();
            return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
        }
    }

    private String rewriteLocation(AppOpen open, String targetUrl, String location) {
        if (location == null || location.isBlank()) {
            return location;
        }
        String prefix = shellPrefix(open.getAppCode());
        if (location.startsWith("/") && !location.startsWith("//")) {
            if (location.startsWith(prefix)) {
                return location;
            }
            return prefix + location;
        }
        try {
            URI loc = URI.create(location);
            URI target = URI.create(targetUrl);
            if (loc.getHost() != null && loc.getHost().equalsIgnoreCase(target.getHost())) {
                String path = loc.getRawPath() == null || loc.getRawPath().isBlank() ? "/" : loc.getRawPath();
                String query = loc.getRawQuery() == null ? "" : "?" + loc.getRawQuery();
                return prefix + path + query;
            }
        } catch (IllegalArgumentException ignored) {
            return location;
        }
        return location;
    }

    private byte[] maybeRewrite(String appCode, String contentType, byte[] body) {
        if (body == null || body.length == 0 || !needRewrite(contentType)) {
            return body;
        }
        String prefix = shellPrefix(appCode);
        String type = contentType.toLowerCase(Locale.ROOT);
        String text = new String(body, StandardCharsets.UTF_8);
        if (type.contains("text/html")) {
            text = rewriteHtmlAttrs(prefix, text);
            text = rewriteCssUrls(prefix, text);
            text = rewriteAssetPaths(prefix, text);
            text = rewriteQuotedVitePaths(prefix, text);
            text = injectHook(prefix, text);
        } else if (type.contains("css")) {
            text = rewriteCssUrls(prefix, text);
        } else {
            text = rewriteQuotedVitePaths(prefix, text);
        }
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private String injectHook(String prefix, String html) {
        String map = "<script type=\"importmap\">{\"imports\":{"
                + "\"/src/\":\"" + prefix + "/src/\","
                + "\"/@vite/\":\"" + prefix + "/@vite/\","
                + "\"/@react-refresh\":\"" + prefix + "/@react-refresh\","
                + "\"/@fs/\":\"" + prefix + "/@fs/\","
                + "\"/@id/\":\"" + prefix + "/@id/\","
                + "\"/node_modules/\":\"" + prefix + "/node_modules/\","
                + "\"/assets/\":\"" + prefix + "/assets/\","
                + "\"/static/\":\"" + prefix + "/static/\""
                + "}}</script>";
        String hook = "<script>(function(){var p='" + prefix
                + "';function same(u){try{return new URL(u,location.href).origin===location.origin;}catch(e){return false;}}"
                + "function fix(u){if(typeof u!=='string'||!u)return u;"
                + "if(u.charAt(0)==='/'&&u.charAt(1)!=='/'&&u.indexOf(p)!==0)return p+u;"
                + "try{var x=new URL(u,location.href);if(x.origin===location.origin&&x.pathname.indexOf(p)!==0)"
                + "{return p+x.pathname+x.search+x.hash;}}catch(e){}return u;}"
                + "var f=window.fetch;window.fetch=function(i,n){if(typeof i==='string')i=fix(i);"
                + "else if(i&&i.url)i=new Request(fix(i.url),i);return f.call(this,i,n);};"
                + "var o=XMLHttpRequest.prototype.open;XMLHttpRequest.prototype.open=function(m,u){"
                + "arguments[1]=fix(u);return o.apply(this,arguments);};"
                + "var sa=Element.prototype.setAttribute;Element.prototype.setAttribute=function(n,v){"
                + "if((n==='src'||n==='href')&&typeof v==='string')v=fix(v);return sa.call(this,n,v);};"
                + "function patch(proto,prop){var d=Object.getOwnPropertyDescriptor(proto,prop);if(!d||!d.set)return;"
                + "Object.defineProperty(proto,prop,{configurable:true,enumerable:d.enumerable,get:d.get,set:function(v){d.set.call(this,fix(v));}});}"
                + "if(window.HTMLScriptElement)patch(HTMLScriptElement.prototype,'src');"
                + "if(window.HTMLLinkElement)patch(HTMLLinkElement.prototype,'href');"
                + "if(window.HTMLIFrameElement)patch(HTMLIFrameElement.prototype,'src');"
                + "if(window.WebSocket){var W=window.WebSocket;window.WebSocket=function(u,r){"
                + "if(typeof u==='string'){try{var x=new URL(u,location.href);"
                + "if(same(u)&&x.pathname.indexOf(p)!==0){"
                + "u=(location.protocol==='https:'?'wss:':'ws:')+'//'+location.host+p+x.pathname+x.search;}"
                + "}catch(e){}}return r===undefined?new W(u):new W(u,r);};window.WebSocket.prototype=W.prototype;}"
                + "})();</script>";
        String inject = map + hook;
        Matcher head = Pattern.compile("(?i)<head[^>]*>").matcher(html);
        if (head.find()) {
            return html.substring(0, head.end()) + inject + html.substring(head.end());
        }
        return inject + html;
    }

    private String rewriteHtmlAttrs(String prefix, String html) {
        String skip = Pattern.quote(prefix.substring(1));
        Pattern attr = Pattern.compile("(?i)(\\s(?:src|href|action|poster)=[\"'])/(?!" + skip + ")");
        return attr.matcher(html).replaceAll("$1" + Matcher.quoteReplacement(prefix) + "/");
    }

    private String rewriteCssUrls(String prefix, String css) {
        String skip = Pattern.quote(prefix.substring(1));
        return css.replaceAll("url\\((['\"]?)/(?!" + skip + ")",
                "url($1" + Matcher.quoteReplacement(prefix) + "/");
    }

    private String rewriteAssetPaths(String prefix, String text) {
        Pattern path = Pattern.compile("(?<!" + Pattern.quote(prefix)
                + ")(/(?:assets/|src/|static/|node_modules/|@vite|@fs|@id|@react-refresh|favicon\\.ico))");
        return path.matcher(text).replaceAll(Matcher.quoteReplacement(prefix) + "$1");
    }

    private String rewriteQuotedVitePaths(String prefix, String text) {
        Pattern quoted = Pattern.compile(
                "(['\"])(/(?:@vite|@react-refresh|@fs|@id|src/|node_modules/|assets/|static/)[^'\"]*)\\1");
        Matcher matcher = quoted.matcher(text);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String path = matcher.group(2);
            String next = path.startsWith(prefix + "/") || path.equals(prefix)
                    ? matcher.group(0)
                    : matcher.group(1) + prefix + path + matcher.group(1);
            matcher.appendReplacement(out, Matcher.quoteReplacement(next));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private boolean needRewrite(String contentType) {
        if (contentType == null) {
            return false;
        }
        String type = contentType.toLowerCase(Locale.ROOT);
        return type.contains("text/html")
                || type.contains("text/css")
                || type.contains("javascript")
                || type.contains("ecmascript")
                || type.contains("typescript");
    }

    private String shellPrefix(String appCode) {
        String code = appCode == null ? "" : appCode.trim();
        return "/ext/" + code;
    }

    private String stubHtml(AppOpen open) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/ext-stub/index.html");
        try (InputStream in = resource.getInputStream()) {
            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            String name = open.getAppName() == null ? "外接模块" : open.getAppName();
            String code = open.getAppCode() == null ? "" : open.getAppCode();
            return html.replace("演示外接模块", name).replace("DEMO_SHELL", code);
        }
    }

    private String downHtml(AppOpen open, String error) {
        String url = open.getTargetUrl() == null ? "" : escape(open.getTargetUrl());
        String name = open.getAppName() == null ? "外接模块" : escape(open.getAppName());
        String code = open.getAppCode() == null ? "" : escape(open.getAppCode());
        String reason = error == null || error.isBlank() ? "模块进程未启动或目标不可达" : escape(error);
        return "<!DOCTYPE html><html lang=\"zh-CN\"><meta charset=\"UTF-8\">"
                + "<body style=\"font-family:-apple-system,'PingFang SC',sans-serif;padding:40px;color:#35586a;background:#f3f7f9\">"
                + "<p style=\"color:#2b8fb6;font-size:12px\">网关未能转到模块</p>"
                + "<h1 style=\"color:#145860\">" + name + "</h1>"
                + "<p>客户路径 <code>/ext/" + code + "/</code> 未跳到模块机。</p>"
                + "<p>网关目标：<code>" + url + "</code></p>"
                + "<p>原因：<code>" + reason + "</code></p>"
                + "</body></html>";
    }

    private String missingHtml(String appCode) {
        return "<!DOCTYPE html><html lang=\"zh-CN\"><meta charset=\"UTF-8\"><body style=\"font-family:sans-serif;padding:40px;color:#35586a\">"
                + "<p>模块未植入入口：" + escape(appCode) + "</p></body></html>";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void writeHtml(HttpServletResponse response, int status, String html) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.getWriter().write(html);
    }
}
