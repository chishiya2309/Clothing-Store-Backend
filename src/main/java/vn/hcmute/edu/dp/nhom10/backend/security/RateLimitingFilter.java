package vn.hcmute.edu.dp.nhom10.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.hcmute.edu.dp.nhom10.backend.config.RateLimitProperties;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String HEADER_LIMIT = "X-RateLimit-Limit";
    private static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_RESET = "X-RateLimit-Reset";
    private static final String HEADER_RULE = "X-RateLimit-Rule";

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final ClientIpResolver clientIpResolver;
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return findMatchingRule(request) == null;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        RateLimitProperties.Rule rule = findMatchingRule(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitDecision decision = rateLimitService.consume(
                buildRateLimitKey(rule, request),
                rule.getLimit(),
                Duration.ofSeconds(rule.getWindowSeconds())
        );

        applyHeaders(response, rule, decision);

        if (!decision.allowed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(decision.retryAfterSeconds()));
            apiErrorResponseWriter.write(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    buildMessage(rule, decision),
                    request.getRequestURI()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitProperties.Rule findMatchingRule(HttpServletRequest request) {
        List<RateLimitProperties.Rule> rules = rateLimitProperties.getRules();
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        for (RateLimitProperties.Rule rule : rules) {
            if (rule == null || rule.getLimit() <= 0 || rule.getWindowSeconds() <= 0) {
                continue;
            }
            if (!matchesMethod(rule, method)) {
                continue;
            }
            if (matchesPath(rule, path)) {
                return rule;
            }
        }

        return null;
    }

    private boolean matchesMethod(RateLimitProperties.Rule rule, String method) {
        List<String> configuredMethods = rule.getMethods();
        if (configuredMethods == null || configuredMethods.isEmpty()) {
            return true;
        }
        return configuredMethods.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .anyMatch(configuredMethod -> configuredMethod.equalsIgnoreCase(method));
    }

    private boolean matchesPath(RateLimitProperties.Rule rule, String path) {
        List<String> pathPatterns = rule.getPathPatterns();
        if (pathPatterns == null || pathPatterns.isEmpty()) {
            return false;
        }
        return pathPatterns.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String buildRateLimitKey(RateLimitProperties.Rule rule, HttpServletRequest request) {
        String ruleKey = StringUtils.hasText(rule.getKey())
                ? rule.getKey().trim()
                : request.getMethod() + ":" + request.getRequestURI();
        return ruleKey + ":" + clientIpResolver.resolve(request);
    }

    private void applyHeaders(HttpServletResponse response, RateLimitProperties.Rule rule, RateLimitDecision decision) {
        if (StringUtils.hasText(rule.getKey())) {
            response.setHeader(HEADER_RULE, rule.getKey().trim());
        }
        response.setHeader(HEADER_LIMIT, String.valueOf(decision.limit()));
        response.setHeader(HEADER_REMAINING, String.valueOf(decision.remaining()));
        response.setHeader(HEADER_RESET, String.valueOf(decision.retryAfterSeconds()));
    }

    private String buildMessage(RateLimitProperties.Rule rule, RateLimitDecision decision) {
        if (StringUtils.hasText(rule.getMessage())) {
            return rule.getMessage().trim();
        }
        return "Too many requests. Please try again in " + decision.retryAfterSeconds() + " seconds.";
    }
}
