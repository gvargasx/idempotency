package org.example.idempotency.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.idempotency.config.IdempotencyProperties;
import org.example.idempotency.model.IdempotencyEntry;
import org.example.idempotency.storage.IdempotencyStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    private final IdempotencyStorage storage;
    private final IdempotencyProperties properties;

    public IdempotencyFilter(IdempotencyStorage storage,
                             IdempotencyProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final Set<String> protectedMethods = properties.getMethods();
        final String method = request.getMethod();

        if (protectedMethods == null || protectedMethods.isEmpty()) {
            LOGGER.warn("idempotency.methods not configured — skipping idempotency filter");
            filterChain.doFilter(request, response);
            return;
        }

        if (!protectedMethods.contains(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String idempotencyKey = request.getHeader("Idempotency-Key");
        if (!StringUtils.hasText(idempotencyKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        String scopedKey = buildScopedKey(method, request, idempotencyKey);

        IdempotencyEntry cached = storage.get(scopedKey);
        if (cached != null) {
            LOGGER.info("idempotency.hit method={} path={} key={}",
                    method, request.getRequestURI(), idempotencyKey);

            writeCachedResponse(response, cached);
            return;
        }

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(cachingRequest, cachingResponse);

        HttpStatusCode status = HttpStatusCode.valueOf(cachingResponse.getStatus());
        Charset encoding = resolveEncoding(cachingResponse.getCharacterEncoding());
        String responseBody = new String(cachingResponse.getContentAsByteArray(), encoding);

        storage.put(scopedKey, new IdempotencyEntry(status, responseBody));

        LOGGER.info("idempotency.store method={} path={} key={} status={}",
                method, request.getRequestURI(), idempotencyKey, status.value());

        cachingResponse.copyBodyToResponse();
    }

    private Charset resolveEncoding(String name) {
        return name != null ? Charset.forName(name) : DEFAULT_ENCODING;
    }

    private void writeCachedResponse(HttpServletResponse response, IdempotencyEntry cached)
            throws IOException {
        response.setStatus(cached.status().value());
        response.setCharacterEncoding(DEFAULT_ENCODING.name());
        response.getWriter().write(cached.responseBody());
    }

    private String buildScopedKey(String method, HttpServletRequest request, String idempotencyKey) {
        return method + ":" + request.getRequestURI() + ":" + idempotencyKey;
    }
}
