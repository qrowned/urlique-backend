package studio.urlique.server.user;

import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final FirebaseAuth firebaseAuth;

    public void setUserRole(String uid, List<UserRole> requestedUserRoles) {
        List<String> permissions = requestedUserRoles
                .stream()
                .map(Enum::toString).toList();

        Map<String, Object> claims = Map.of("role", permissions);

        this.firebaseAuth.setCustomUserClaimsAsync(uid, claims);
    }

}
