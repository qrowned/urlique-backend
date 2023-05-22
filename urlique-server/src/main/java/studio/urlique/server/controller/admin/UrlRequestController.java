package studio.urlique.server.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import studio.urlique.api.RequestResult;
import studio.urlique.api.url.UrlRequestData;
import studio.urlique.server.url.request.UrlRequestService;

import java.util.concurrent.Future;

@RestController
@RequestMapping("/admin/requests")
@RequiredArgsConstructor
public class UrlRequestController {

    private final UrlRequestService urlRequestService;

    @Async
    @GetMapping("/{urlId}")
    public Future<RequestResult<UrlRequestData>> fetchUser(@PathVariable String urlId) {
        return this.urlRequestService.fetchUrlRequestData(urlId);
    }

    @Async
    @PatchMapping("/{urlId}/increase")
    public Future<RequestResult<UrlRequestData>> increaseRequests(@PathVariable String urlId) {
        return this.urlRequestService.increaseRequests(urlId);
    }

}
