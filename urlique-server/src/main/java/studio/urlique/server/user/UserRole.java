package studio.urlique.server.user;

import com.google.firebase.auth.UserRecord;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

/**
 * Represents a Role of a Firebase {@link UserRecord}.
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    USER(100,
            Bandwidth.classic(20, Refill.intervally(10, Duration.ofMinutes(1)))
    ),
    MODERATOR(50,
            Bandwidth.classic(100, Refill.intervally(20, Duration.ofMinutes(1)))
    ),
    ADMIN(10,
            Bandwidth.classic(500, Refill.intervally(100, Duration.ofMinutes(1)))
    );

    private final int priority;
    private final Bandwidth limit;

}
