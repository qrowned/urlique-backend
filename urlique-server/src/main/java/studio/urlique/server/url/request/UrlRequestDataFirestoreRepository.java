package studio.urlique.server.url.request;

import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;
import studio.urlique.api.database.AbstractFirestoreRepository;
import studio.urlique.api.url.UrlRequestData;

@Repository
public class UrlRequestDataFirestoreRepository extends AbstractFirestoreRepository<UrlRequestData> {

    protected UrlRequestDataFirestoreRepository(Firestore firestore) {
        super(firestore, "url_requests");
    }

}
