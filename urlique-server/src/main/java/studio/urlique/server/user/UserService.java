package studio.urlique.server.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import studio.urlique.api.RequestResult;
import studio.urlique.api.utils.FutureUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class UserService {

    private final FirebaseAuth firebaseAuth;

    /**
     * Add a {@link UserRole} to a Firebase user.
     *
     * @param uid               unique ID of the Firebase user.
     * @param requestedUserRole requested role which should be added.
     * @return result of the user update.
     */
    public CompletableFuture<RequestResult<UserRecord>> addUserRole(String uid, UserRole requestedUserRole) {
        return this.fetchUserRecord(uid).thenComposeAsync(userRecordResult -> {
            if (!userRecordResult.isSuccess())
                return CompletableFuture.completedFuture(userRecordResult);

            UserRecord userRecord = userRecordResult.getResult();
            List<String> userRoles = userRecord.getCustomClaims().get("roles") == null
                    ? new ArrayList<>()
                    : (List<String>) userRecord.getCustomClaims().get("roles");

            String userRoleString = requestedUserRole.toString();
            if (!userRoles.contains(userRoleString))
                userRoles.add(userRoleString);

            Map<String, Object> claims = Map.of("roles", userRoles);
            return this.updateUserClaims(uid, claims);
        });
    }

    /**
     * Remove a {@link UserRole} from a Firebase user.
     *
     * @param uid               unique ID of the Firebase user.
     * @param requestedUserRole requested role which should be removed.
     * @return result of the user update.
     */
    public CompletableFuture<RequestResult<UserRecord>> removeUserRole(String uid, UserRole requestedUserRole) {
        return this.fetchUserRecord(uid).thenComposeAsync(userRecordResult -> {
            if (!userRecordResult.isSuccess())
                return CompletableFuture.completedFuture(userRecordResult);

            UserRecord userRecord = userRecordResult.getResult();
            List<String> roles = (List<String>) userRecord.getCustomClaims().get("roles");
            if (roles == null) return CompletableFuture.completedFuture(RequestResult.ok(userRecord));

            roles.remove(requestedUserRole.toString());

            Map<String, Object> claims = Map.of("roles", roles);
            return this.updateUserClaims(uid, claims);
        });
    }

    /**
     * Update the internal Firebase user claims.
     *
     * @param uid    unique ID of the Firebase user.
     * @param claims map of all the user claims.
     * @return result of the user update.
     */
    private CompletableFuture<RequestResult<UserRecord>> updateUserClaims(String uid, Map<String, Object> claims) {
        return FutureUtils.toCompletableFuture(this.firebaseAuth.setCustomUserClaimsAsync(uid, claims))
                .thenComposeAsync(unused ->
                        FutureUtils.toCompletableFuture(this.firebaseAuth.getUserAsync(uid))
                                .thenApplyAsync(RequestResult::ok)
                );
    }

    /**
     * Fetch a certain record of a Firebase user.
     * "record" means general information from Firebase AUTH.
     *
     * @param uid unique ID of the Firebase user.
     * @return result of the fetch request.
     */
    public CompletableFuture<RequestResult<UserRecord>> fetchUserRecord(@NotNull String uid) {
        return FutureUtils.toCompletableFuture(this.firebaseAuth.getUserAsync(uid))
                .thenApplyAsync(userRecord -> {
                    if (userRecord == null)
                        return RequestResult.error("auth.user.notExisting");

                    return RequestResult.ok(userRecord);
                });
    }

    /**
     * Get all roles of a certain {@link UserRecord}.
     *
     * @param userRecord userRecord object to fetch roles from.
     * @return parsed roles.
     */
    public List<UserRole> getUserRoles(@NotNull UserRecord userRecord) {
        return ((List<String>) userRecord.getCustomClaims().getOrDefault("roles", new ArrayList<String>()))
                .stream().map(UserRole::valueOf)
                .toList();
    }

    /**
     * Get the role with the highest priority from a {@link UserRecord}.
     *
     * @param userRecord userRecord object to fetch role from.
     * @return the highest parsed role.
     */
    public UserRole getHighestRole(@NotNull UserRecord userRecord) {
        return this.getUserRoles(userRecord).stream()
                .min(Comparator.comparingInt(UserRole::getPriority))
                .orElse(UserRole.USER);
    }

}
