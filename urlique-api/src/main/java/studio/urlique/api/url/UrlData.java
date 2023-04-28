package studio.urlique.api.url;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record UrlData(@NotNull String id,
                      @NotNull String url,
                      @NotNull Instant createdAt,
                      @NotNull String creator) {

    public UrlData(@NotNull String id,
                   @NotNull String url,
                   @NotNull String creator) {
        this(id, url, Instant.now(), creator);
    }

}
