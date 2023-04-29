package studio.urlique.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlData;
import studio.urlique.server.url.UrlDataService;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlDataService urlDataService;

    @Async
    @GetMapping("{id}") // using "/**" to allow access with slash and without slash
    public Future<RequestResult<UrlData>> fetch(@PathVariable String id, Principal principal) {
        return this.urlDataService.fetchUrlDataEntry(id, principal);
    }

    @Async
    @GetMapping("list")
    public Future<RequestResult<List<UrlData>>> fetchAll(@RequestParam(defaultValue = "25") int pageSize,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         Principal principal) {
        return this.urlDataService.fetchUrlDataEntries(principal, pageSize, page);
    }

    @Async
    @PostMapping("create")
    public Future<RequestResult<UrlData>> create(@RequestParam URI url, Principal principal) throws MalformedURLException {
        return this.urlDataService.createUrlDataEntry(url.toURL().toString(), principal.getName());
    }

    @Async
    @PostMapping("createWithId")
    public Future<RequestResult<UrlData>> create(@RequestParam String id, @RequestParam URI url,
                                                 Principal principal) throws MalformedURLException {
        return this.urlDataService.createUrlDataEntry(id, url.toURL().toString(), principal.getName());
    }

    @Async
    @DeleteMapping("delete")
    public Future<RequestResult<UrlData>> delete(@RequestParam String id, Principal principal) {
        return this.urlDataService.deleteUrlDataEntry(id, principal);
    }

}
