package org.example.idempotency.filter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1)))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ip = req.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(ip, key -> createBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
            return;
        }

        LOGGER.warn("rate_limit_exceeded ip={} method={} path={}",
                ip, req.getMethod(), req.getRequestURI());

        resp.setStatus(429);
        resp.setContentType("application/json");
        resp.getWriter().write("""
                    {
                      "error": "rate_limit_exceeded",
                      "message": "Too many requests. Try again later."
                    }
                """);
    }
}
