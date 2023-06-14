package studio.urlique.server.user;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

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
