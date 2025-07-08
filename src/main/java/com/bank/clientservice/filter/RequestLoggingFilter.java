package com.bank.clientservice.filter;

import com.bank.clientservice.entity.ApiMetric;
import com.bank.clientservice.repository.ApiMetricRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final ApiMetricRepository apiMetricRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip static resources and monitoring endpoints
        String path = request.getRequestURI();
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String errorMessage = null;

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;

            // Save API metric
            try {
                ApiMetric metric = ApiMetric.builder()
                        .timestamp(LocalDateTime.now())
                        .endpoint(path)
                        .method(request.getMethod())
                        .statusCode(wrappedResponse.getStatus())
                        .responseTimeMs((int) responseTime)
                        .requestSizeBytes((long) wrappedRequest.getContentLengthLong())
                        .responseSizeBytes((long) wrappedResponse.getContentSize())
                        .userAgent(request.getHeader("User-Agent"))
                        .ipAddress(getClientIpAddress(request))
                        .errorMessage(errorMessage)
                        .build();

                apiMetricRepository.save(metric);

                // Log slow requests
                if (responseTime > 1000) {
                    log.warn("Slow request: {} {} took {}ms",
                            request.getMethod(), path, responseTime);
                }

            } catch (Exception e) {
                log.error("Error saving API metric", e);
            }

            // Copy response body to actual response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean shouldSkip(String path) {
        return path.contains("/static/") ||
                path.contains("/css/") ||
                path.contains("/js/") ||
                path.contains("/images/") ||
                path.contains("/favicon") ||
                path.contains("/monitoring/api/metrics/stream") || // Skip SSE endpoint
                path.contains("/api/metrics/stream"); // Skip all SSE endpoints
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}