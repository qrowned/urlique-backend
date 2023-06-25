package studio.urlique.server.controller;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlData;
import studio.urlique.server.url.UrlDataService;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlDataService urlDataService;

    @Async
    @GetMapping("/{id}/info") // using "/**" to allow access with slash and without slash
    public Future<RequestResult<UrlData>> fetch(@PathVariable String id, @Nullable Principal principal) {
        return this.urlDataService.fetchUrlDataEntry(id).thenApplyAsync(urlDataRequestResult -> {
            if (!urlDataRequestResult.isSuccess()) return urlDataRequestResult;

            UrlData urlData = urlDataRequestResult.getResult();
            if (principal == null || !urlData.equalsCreator(principal.getName())) {
                this.urlDataService.increaseRequests(id);
                urlData.increaseRequest();
            }

            return RequestResult.ok(urlData);
        });
    }

    @Async
    @GetMapping("/list")
    public Future<RequestResult<List<UrlData>>> fetchAll(@RequestParam(defaultValue = "25") int pageSize,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @NotNull Principal principal) {
        return this.urlDataService.fetchUrlDataEntries(principal, pageSize, page);
    }

    @Async
    @PostMapping("/create")
    public Future<RequestResult<UrlData>> create(@RequestParam URI url, @Nullable Principal principal) {
        try {
            return this.urlDataService.createUrlDataEntry(url.toURL().toString(), principal);
        } catch (MalformedURLException exception) {
            return CompletableFuture.completedFuture(RequestResult.error("url.invalid"));
        }
    }

    @Async
    @PostMapping("/createWithId")
    public Future<RequestResult<UrlData>> create(@RequestParam String id, @RequestParam URI url,
                                                 @NotNull Principal principal) {
        try {
            return this.urlDataService.createUrlDataEntry(id, url.toURL().toString(), principal);
        } catch (MalformedURLException exception) {
            return CompletableFuture.completedFuture(RequestResult.error("url.invalid"));
        }
    }

    @Async
    @DeleteMapping("{id}/delete")
    public Future<RequestResult<UrlData>> delete(@PathVariable String id, @NotNull Principal principal) {
        return this.urlDataService.deleteUrlDataEntry(id, principal);
    }

}
