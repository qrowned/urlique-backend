package studio.urlique.server.limit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.security.Principal;

@Component
@CommonsLog
@RequiredArgsConstructor
public final class RateLimitFilter extends GenericFilterBean {

    private final RoleLimitService roleLimitService;

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ipAddress = forwardedFor == null ? request.getRemoteAddr() : forwardedFor.split(",")[0];

        Principal principal = request.getUserPrincipal();
        Bucket bucket = principal != null
                ? this.roleLimitService.resolveBucket(principal)
                : this.roleLimitService.resolveBucket(ipAddress);

        log.info("Received request from IP-Address <" + ipAddress + "> with " + bucket.getAvailableTokens() + " tokens left. (Signed in: " + (principal != null) + ")");

        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
        if (consumptionProbe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(consumptionProbe.getRemainingTokens()));
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            long waitForRefill = consumptionProbe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "You've reached your Request quota.");
        }

    }

}
