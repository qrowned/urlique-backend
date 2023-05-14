package studio.urlique.server.url;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlData;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public final class UrlDataService {

    private final UrlDataFirestoreRepository urlDataFirestoreRepository;

    /**
     * Create a URL data entry without specific ID.
     *
     * @see UrlDataService#createUrlDataEntry(String, String, Principal)
     */
    public CompletableFuture<RequestResult<UrlData>> createUrlDataEntry(@NotNull String url,
                                                                        @Nullable Principal creator) {
        return this.createUrlDataEntry(RandomStringUtils.randomAlphanumeric(6), url, creator);
    }

    /**
     * Create a URL data entry with specific ID.
     *
     * @param id specific ID of short link.
     * @param url URL the short link should point to.
     * @param principal creator of the entry/request.
     * @return result of create request.
     */
    public CompletableFuture<RequestResult<UrlData>> createUrlDataEntry(@NotNull String id,
                                                                        @NotNull String url,
                                                                        @Nullable Principal principal) {
        return this.urlDataFirestoreRepository.get(id).thenComposeAsync(existingData -> {
            if (existingData.isPresent()) return this.createUrlDataEntry(url, principal);

            UrlData urlData = new UrlData(id, url, principal == null ? null : principal.getName());
            this.urlDataFirestoreRepository.save(urlData);
            return CompletableFuture.supplyAsync(() -> RequestResult.ok(urlData));
        });
    }

    /**
     * Fetch a URL data entry by its ID.
     *
     * @param id ID of URL data entry.
     * @return result of fetch request.
     */
    public CompletableFuture<RequestResult<UrlData>> fetchUrlDataEntry(@NotNull String id) {
        return this.urlDataFirestoreRepository.get(id).thenApplyAsync(urlDataOptional -> {
            return urlDataOptional.map(RequestResult::ok).orElseGet(() -> RequestResult.error("url.id.notFound"));
        });
    }

    /**
     * Fetch all URL data entries of a certain creator.
     * To prevent memory overload, a pagination system is implemented.
     *
     * @param creator creator to get the URL data entries from.
     * @param pageSize how many entries should be on one page.
     * @param page specific page to fetch from.
     * @return result of fetch request.
     */
    public CompletableFuture<RequestResult<List<UrlData>>> fetchUrlDataEntries(@NotNull Principal creator,
                                                                               int pageSize, int page) {
        if (pageSize > 50) return CompletableFuture.completedFuture(RequestResult.error("url.pageSize.tooLarge"));
        return this.urlDataFirestoreRepository.retrieveAllByCreator(creator.getName(), pageSize, page).thenApplyAsync(RequestResult::ok);
    }

    /**
     * Delete a specific URL data entry.
     *
     * @param id ID of URL data entry which should be deleted.
     * @param principal identity who executed the request.
     * @return result of delete request.
     */
    public CompletableFuture<RequestResult<UrlData>> deleteUrlDataEntry(@NotNull String id,
                                                                        @NotNull Principal principal) {
        return this.urlDataFirestoreRepository.get(id).thenApplyAsync(urlDataOptional -> {
            if (urlDataOptional.isEmpty()) return RequestResult.error("url.id.notFound");

            UrlData urlData = urlDataOptional.get();
            if (!urlData.equalsCreator(principal.getName()))
                return RequestResult.error("url.action.noPermission");

            this.urlDataFirestoreRepository.delete(id);
            return RequestResult.ok(urlData);
        });
    }

}
