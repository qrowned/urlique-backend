package studio.urlique.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlData;
import studio.urlique.server.url.UrlDataService;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlDataService urlDataService;

    @Async
    @GetMapping("{id}/**") // using "/**" to allow access with slash and without slash
    public Future<RequestResult<UrlData>> fetch(@PathVariable String id) {
        return this.urlDataService.fetchUrlDataEntry(id);
    }

    @Async
    @PostMapping("create/")
    public Future<RequestResult<UrlData>> create(@RequestParam URI url,
                                                 @RequestHeader("API-KEY") String apiKey) throws MalformedURLException {
        return this.urlDataService.createUrlDataEntry(url.toURL().toString(), apiKey);
    }

    @Async
    @PostMapping("createWithId/")
    public Future<RequestResult<UrlData>> create(@RequestParam String id, @RequestParam URI url,
                                                 @RequestHeader("API-KEY") String apiKey) throws MalformedURLException {
        return this.urlDataService.createUrlDataEntry(id, url.toURL().toString(), apiKey);
    }

    @Async
    @DeleteMapping("delete/")
    public void delete(@RequestParam String id) {
        this.urlDataService.deleteUrlDataEntry(id);
    }

}
