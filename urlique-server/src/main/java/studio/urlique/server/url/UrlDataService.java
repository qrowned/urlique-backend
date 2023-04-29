package studio.urlique.server.url;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlData;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public final class UrlDataService {

    private final UrlDataFirestoreRepository urlDataFirestoreRepository;

    public CompletableFuture<RequestResult<UrlData>> createUrlDataEntry(@NotNull String url,
                                                                        @NotNull String creator) {
        return this.createUrlDataEntry(RandomStringUtils.randomAlphanumeric(5), url, creator);
    }

    public CompletableFuture<RequestResult<UrlData>> createUrlDataEntry(@NotNull String id,
                                                                        @NotNull String url,
                                                                        @NotNull String creator) {
        return this.urlDataFirestoreRepository.get(id).thenComposeAsync(existingData -> {
            if (existingData.isPresent()) return this.createUrlDataEntry(url, creator);

            UrlData urlData = new UrlData(id, url, creator);
            this.urlDataFirestoreRepository.save(urlData);
            return CompletableFuture.supplyAsync(() -> RequestResult.ok(urlData));
        });
    }

    public CompletableFuture<RequestResult<UrlData>> fetchUrlDataEntry(@NotNull String id) {
        return this.urlDataFirestoreRepository.get(id).thenApplyAsync(urlData -> {
            if (urlData.isEmpty()) return RequestResult.error("url.id.notFound");
            return RequestResult.ok(urlData.get());
        });
    }

    public CompletableFuture<RequestResult<List<UrlData>>> fetchUrlDataEntries(int pageSize, int page) {
        if (pageSize > 50) return CompletableFuture.completedFuture(RequestResult.error("url.pageSize.tooLarge"));
        return this.urlDataFirestoreRepository.retrieveAllLimit(pageSize, page).thenApplyAsync(RequestResult::ok);
    }

    public void deleteUrlDataEntry(@NotNull String id) {
        this.urlDataFirestoreRepository.delete(id);
    }

}
