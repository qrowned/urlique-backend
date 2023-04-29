package studio.urlique.server.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final FirebaseAuth firebaseAuth;

    public void setUserClaims(String uid, List<Permission> requestedPermissions) throws FirebaseAuthException {
        List<String> permissions = requestedPermissions
                .stream()
                .map(Enum::toString).toList();

        Map<String, Object> claims = Map.of("custom_claims", permissions);

        this.firebaseAuth.setCustomUserClaims(uid, claims);
    }

}
