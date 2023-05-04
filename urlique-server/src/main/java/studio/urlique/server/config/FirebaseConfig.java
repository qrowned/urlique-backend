package studio.urlique.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private final GoogleCredentials credentials;

    public FirebaseConfig(@Value("${firebase.credential.path}") String credentialPath) throws IOException {
        var serviceAccount = new FileInputStream(credentialPath);
        this.credentials = GoogleCredentials.fromStream(serviceAccount);
    }

    @Bean
    public Firestore firestore() {
        var options = FirestoreOptions.newBuilder()
                .setCredentials(this.credentials).build();
        return options.getService();
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        var options = FirebaseOptions.builder()
                .setCredentials(this.credentials)
                .build();

        var firebaseApp = FirebaseApp.initializeApp(options);

        return FirebaseAuth.getInstance(firebaseApp);
    }

}