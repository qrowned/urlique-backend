package studio.urlique.server.url.request;

import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlRequestData;
import studio.urlique.server.url.UrlDataService;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public final class UrlRequestService {

    private final UrlDataService urlDataService;
    private final UrlRequestDataFirestoreRepository urlRequestDataFirestoreRepository;

    public CompletableFuture<RequestResult<UrlRequestData>> fetchUrlRequestData(@NotNull String urlId) {
        return this.urlRequestDataFirestoreRepository.get(urlId).thenApplyAsync(urlRequestData -> {
            return urlRequestData.map(RequestResult::ok).orElseGet(() -> RequestResult.error("url.id.notFound"));
        });
    }

    public CompletableFuture<RequestResult<UrlRequestData>> increaseRequests(@NotNull String urlId) {
        return this.fetchUrlRequestData(urlId).thenComposeAsync(urlRequest -> {
            if (!urlRequest.isSuccess()) {
                return this.urlDataService.fetchUrlDataEntry(urlId).thenApplyAsync(urlDataRequestResult -> {
                    if (!urlDataRequestResult.isSuccess()) return RequestResult.error("url.id.notFound");

                    return RequestResult.ok(this.createFromEmpty(urlId));
                });
            }

            UrlRequestData urlRequestData = urlRequest.getResult();
            urlRequestData.increaseRequest();

            this.urlRequestDataFirestoreRepository.save(urlRequestData);
            return CompletableFuture.completedFuture(RequestResult.ok(urlRequestData));
        });
    }

    private UrlRequestData createFromEmpty(@NotNull String id) {
        UrlRequestData urlRequestData = new UrlRequestData(id, 1, Timestamp.now());
        this.urlRequestDataFirestoreRepository.save(urlRequestData);
        return urlRequestData;
    }

}
