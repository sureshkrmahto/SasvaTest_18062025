package com.acme.javamigrator.server.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private static class Counter {
        volatile long windowStartSeconds;
        volatile int count;
    }

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    private record Limits(int limitPerMinute, int burst) {}

    private Limits limitsFor(String path) {
        if (path.startsWith("/api/v1/auth")) return new Limits(100, 20);
        if (path.startsWith("/api/v1/projects/upload")) return new Limits(20, 10);
        if (path.startsWith("/api/v1/analysis")) return new Limits(50, 15);
        if (path.startsWith("/api/v1/migration")) return new Limits(10, 5);
        if (path.startsWith("/api/v1/reports")) return new Limits(200, 30);
        return new Limits(1000, 150);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String key = req.getMethod() + ":" + req.getRequestURI() + ":" + req.getRemoteAddr();
        Limits limits = limitsFor(req.getRequestURI());
        long now = Instant.now().getEpochSecond();
        Counter c = counters.computeIfAbsent(key, k -> {
            Counter nc = new Counter(); nc.windowStartSeconds = now; nc.count = 0; return nc; });
        synchronized (c) {
            if (now - c.windowStartSeconds >= 60) {
                c.windowStartSeconds = now;
                c.count = 0;
            }
            if (c.count >= limits.limitPerMinute + limits.burst) {
                resp.setStatus(429);
                resp.setHeader("X-RateLimit-Limit", String.valueOf(limits.limitPerMinute));
                resp.setHeader("X-RateLimit-Remaining", "0");
                resp.setHeader("X-RateLimit-Reset", String.valueOf(c.windowStartSeconds + 60));
                resp.setHeader("X-RateLimit-Retry-After", "60");
                resp.getWriter().write("{\"success\":false,\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests\"}}\n");
                return;
            }
            c.count++;
            resp.setHeader("X-RateLimit-Limit", String.valueOf(limits.limitPerMinute));
            resp.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, (limits.limitPerMinute + limits.burst) - c.count)));
            resp.setHeader("X-RateLimit-Reset", String.valueOf(c.windowStartSeconds + 60));
        }
        chain.doFilter(request, response);
    }
}
