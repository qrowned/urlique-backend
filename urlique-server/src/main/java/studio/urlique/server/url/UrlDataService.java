package studio.urlique.server.url;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlData;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public final class UrlDataService {

    private final UrlDataHandler urlDataHandler;

    public CompletableFuture<RequestResult<UrlData>> createUrlDataEntry(@NotNull String url,
                                                                        @NotNull String creator) {
        return this.createUrlDataEntry(RandomStringUtils.randomAlphanumeric(5), url, creator);
    }

    public CompletableFuture<RequestResult<UrlData>> createUrlDataEntry(@NotNull String id,
                                                                        @NotNull String url,
                                                                        @NotNull String creator) {
        return this.urlDataHandler.fetchUrlData(id).thenComposeAsync(existingData -> {
            if (existingData != null) return this.createUrlDataEntry(url, creator);

            UrlData urlData = new UrlData(id, url, creator);
            this.urlDataHandler.insertData(urlData);
            return CompletableFuture.supplyAsync(() -> RequestResult.ok(urlData));
        });
    }

    public CompletableFuture<RequestResult<UrlData>> fetchUrlDataEntry(@NotNull String id) {
        return this.urlDataHandler.fetchUrlData(id).thenApplyAsync(urlData -> {
            if (urlData == null) return RequestResult.error("url.id.notFound");
            return RequestResult.ok(urlData);
        });
    }

    public void deleteUrlDataEntry(@NotNull String id) {
        this.urlDataHandler.deleteData(id);
    }

}
