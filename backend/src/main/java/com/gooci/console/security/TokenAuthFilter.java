package com.gooci.console.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * @ClassName TokenAuthFilter
 * @Description 解析 Bearer Token
 * @author GooCi
 * @date 2026-03-23
 */
@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final TokenSupport tokenSupport;

    public TokenAuthFilter(TokenSupport tokenSupport) {
        this.tokenSupport = tokenSupport;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    String token = header.substring("Bearer ".length());
                    String userId = tokenSupport.parseUserId(token);
                    String scope = tokenSupport.parseScope(token);
                    String path = request.getServletPath();
                    if (path == null || path.isBlank()) {
                        path = request.getRequestURI();
                    }
                    boolean opsApi = path.startsWith("/api/ops");
                    boolean enterApi = path.startsWith("/api/") && !opsApi
                            && !path.startsWith("/api/site") && !path.startsWith("/api/auth");
                    if ((opsApi && !"ops".equals(scope)) || (enterApi && "ops".equals(scope))) {
                        SecurityContextHolder.clearContext();
                    } else {
                        LoginUserHolder.setUserId(userId);
                        LoginUserHolder.setScope(scope);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception ignored) {
                    SecurityContextHolder.clearContext();
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            LoginUserHolder.clear();
        }
    }
}
