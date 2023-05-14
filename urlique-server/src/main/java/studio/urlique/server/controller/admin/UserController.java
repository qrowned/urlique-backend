package studio.urlique.server.controller.admin;

import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import studio.urlique.api.RequestResult;
import studio.urlique.server.user.UserRole;
import studio.urlique.server.user.UserService;

import java.util.concurrent.Future;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Async
    @PatchMapping("/{uid}/addRole")
    public Future<RequestResult<UserRecord>> addRole(@PathVariable String uid,
                                                     @RequestParam UserRole role) {
        return this.userService.addUserRole(uid, role);
    }

    @Async
    @PatchMapping("/{uid}/removeRole")
    public Future<RequestResult<UserRecord>> removeRole(@PathVariable String uid,
                                                        @RequestParam UserRole role) {
        return this.userService.removeUserRole(uid, role);
    }

    @Async
    @GetMapping("/{uid}")
    public Future<RequestResult<UserRecord>> fetchUser(@PathVariable String uid) {
        return this.userService.fetchUserRecord(uid);
    }

}
