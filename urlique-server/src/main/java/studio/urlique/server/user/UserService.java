package studio.urlique.server.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import studio.urlique.api.RequestResult;
import studio.urlique.api.utils.FutureUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserService {

    private final FirebaseAuth firebaseAuth;

    public CompletableFuture<RequestResult<UserRecord>> setUserRole(String uid, UserRole requestedUserRole) {
        return this.fetchUserRecord(uid).thenComposeAsync(userRecordResult -> {
            if (!userRecordResult.isSuccess())
                return CompletableFuture.completedFuture(userRecordResult);

            UserRecord userRecord = userRecordResult.getResult();
            List<String> userRoles = userRecord.getCustomClaims().get("roles") == null
                    ? new ArrayList<>()
                    : (List<String>) userRecord.getCustomClaims().get("roles");
            userRoles.add(requestedUserRole.toString());

            Map<String, Object> claims = Map.of("roles", userRoles);

            return FutureUtils.toCompletableFuture(this.firebaseAuth.setCustomUserClaimsAsync(uid, claims))
                    .thenComposeAsync(unused ->
                            FutureUtils.toCompletableFuture(this.firebaseAuth.getUserAsync(uid))
                                    .thenApplyAsync(RequestResult::ok)
                    );
        });
    }

    public CompletableFuture<RequestResult<UserRecord>> fetchUserRecord(@NotNull String uid) {
        return FutureUtils.toCompletableFuture(this.firebaseAuth.getUserAsync(uid))
                .thenApplyAsync(userRecord -> {
                    if (userRecord == null)
                        return RequestResult.error("auth.user.notExisting");

                    return RequestResult.ok(userRecord);
                });
    }

}
