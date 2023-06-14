package studio.urlique.server.limit;

import com.google.firebase.auth.UserRecord;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import studio.urlique.api.RequestResult;
import studio.urlique.server.user.UserService;

import java.security.Principal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public final class RoleLimitService {

    private final UserService userService;
    private final Map<String, Bucket> bucketCache = new HashMap<>();

    /**
     * Resolve the Token {@link Bucket} for a {@link Principal}.
     *
     * @param principal the user principal.
     * @return the resolved Bucket for the user (principal).
     */
    public Bucket resolveBucket(@NotNull Principal principal) {
        return this.bucketCache.computeIfAbsent(principal.getName(), s ->
                this.newBucket(principal)
        );
    }

    /**
     * Resolve the Token {@link Bucket} for an IP-Address.
     *
     * @param ipAddress the IP-Address.
     * @return the resolved Bucket for the IP-Address.
     */
    public Bucket resolveBucket(@NotNull String ipAddress) {
        return this.bucketCache.computeIfAbsent(ipAddress, s -> this.newAnonymousBucket());
    }

    /**
     * Create a new {@link Bucket} for a {@link Principal}.
     *
     * @param principal the user principal.
     * @return the created Bucket.
     */
    private Bucket newBucket(@NotNull Principal principal) {
        RequestResult<UserRecord> requestResult = this.userService.fetchUserRecord(principal.getName()).join();
        if (!requestResult.isSuccess())
            throw new UnsupportedOperationException("User cannot be null!");

        return Bucket.builder()
                .addLimit(this.userService.getHighestRole(requestResult.getResult()).getLimit())
                .build();
    }

    /**
     * Create a new {@link Bucket} for an anonymous user.
     *
     * @return the created Bucket.
     */
    private Bucket newAnonymousBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5))))
                .build();
    }

}
