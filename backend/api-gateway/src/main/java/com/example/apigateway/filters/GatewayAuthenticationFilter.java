package com.example.apigateway.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header format");
            return;
        }

        String jwt = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            return;
        }

        if (username != null && !jwtService.isTokenExpired(jwt)) {
            String userIdForHeader = jwtService.extractUserId(jwt).toString();
            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {
                private final Map<String, String> customHeaders = Map.of(
                        "X-User-Id", userIdForHeader,
                        "X-Username", username
                );
                @Override
                public String getHeader(String name) {
                    String headerValue = customHeaders.get(name);
                    if(headerValue != null) {
                        return headerValue;
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    Enumeration<String> originalHeaderNames = super.getHeaderNames();
                    Set<String> headerNames = new LinkedHashSet<>(customHeaders.keySet());
                    while(originalHeaderNames.hasMoreElements()) {
                        headerNames.add(originalHeaderNames.nextElement());
                    }
                    return Collections.enumeration(headerNames);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    String headerValue = customHeaders.get(name);
                    if(headerValue != null) {
                        return Collections.enumeration(List.of(headerValue));
                    }
                    return super.getHeaders(name);
                }
            };
            filterChain.doFilter(wrappedRequest, response);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Expired Token");
        }
    }
}
