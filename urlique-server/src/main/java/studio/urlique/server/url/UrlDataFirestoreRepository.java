package studio.urlique.server.url;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.springframework.stereotype.Repository;
import studio.urlique.api.database.AbstractFirestoreRepository;
import studio.urlique.api.url.UrlData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class UrlDataFirestoreRepository extends AbstractFirestoreRepository<UrlData> {

    protected UrlDataFirestoreRepository(Firestore firestore) {
        super(firestore, "url_data");
    }

    public CompletableFuture<List<UrlData>> retrieveAllLimit(int size, int page) {
        Query query = super.collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(size);
        if (page > 1) {
            query = query.offset((page - 1) * size);
        }
        return super.toCompletableFuture(query.get()).thenApplyAsync(queryDocumentSnapshots -> {
            return queryDocumentSnapshots.toObjects(UrlData.class);
        });
    }
}
