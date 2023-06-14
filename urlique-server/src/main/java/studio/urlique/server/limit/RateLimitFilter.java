package studio.urlique.server.limit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;

/**
 * Represents a filter to limit the requests of any User/IP-Address.
 */
@Component
@CommonsLog
@RequiredArgsConstructor
public final class RateLimitFilter extends OncePerRequestFilter {

    private final RoleLimitService roleLimitService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (this.isPreflightRequest(request)) return;

        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ipAddress = forwardedFor == null ? request.getRemoteAddr() : forwardedFor.split(",")[0];

        Principal principal = request.getUserPrincipal();
        Bucket bucket = principal != null
                ? this.roleLimitService.resolveBucket(principal)
                : this.roleLimitService.resolveBucket(ipAddress);

        log.info("Received request from IP-Address " + ipAddress + " with " + bucket.getAvailableTokens() + " tokens left. (Signed in: " + (principal != null) + ")");

        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
        if (consumptionProbe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(consumptionProbe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = consumptionProbe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "You've reached your Request quota.");
        }
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod()) && request.getHeader("Access-Control-Request-Method") != null;
    }

}