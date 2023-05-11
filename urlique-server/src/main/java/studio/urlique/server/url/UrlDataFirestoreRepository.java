package studio.urlique.server.url;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import studio.urlique.api.database.AbstractFirestoreRepository;
import studio.urlique.api.url.UrlData;
import studio.urlique.api.utils.FutureUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class UrlDataFirestoreRepository extends AbstractFirestoreRepository<UrlData> {

    protected UrlDataFirestoreRepository(Firestore firestore) {
        super(firestore, "url_data");
    }

    public CompletableFuture<List<UrlData>> retrieveAll(int size, int page) {
        Query query = super.collectionReference.orderBy("createdAt", Query.Direction.DESCENDING);
        return this.applyPagination(query, size, page);
    }

    public CompletableFuture<List<UrlData>> retrieveAllByCreator(@NotNull String id, int size, int page) {
        Query query = super.collectionReference.whereEqualTo("creator", id)
                .orderBy("createdAt", Query.Direction.DESCENDING);
        return this.applyPagination(query, size, page);
    }

    private CompletableFuture<List<UrlData>> applyPagination(@NotNull Query query, int size, int page) {
        query = query.limit(size)
                .offset((page - 1) * size);

        return FutureUtils.toCompletableFuture(query.get()).thenApplyAsync(queryDocumentSnapshots -> {
            return queryDocumentSnapshots.toObjects(UrlData.class);
        });
    }

}
