package studio.urlique.api.database;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractFirestoreRepository<T> {

    protected final CollectionReference collectionReference;
    protected final String collectionName;
    protected final Class<T> parameterizedType;

    protected AbstractFirestoreRepository(Firestore firestore, String collection) {
        this.collectionReference = firestore.collection(collection);
        this.collectionName = collection;
        this.parameterizedType = getParameterizedType();
    }

    private Class<T> getParameterizedType() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }

    public CompletableFuture<Boolean> save(T model) {
        String documentId = getDocumentId(model);
        ApiFuture<WriteResult> resultApiFuture = this.collectionReference.document(documentId).set(model);

        return this.toCompletableFuture(resultApiFuture).thenApplyAsync(writeResult -> {
            if (writeResult == null) {
                log.error("Error saving {}={}", this.collectionName, documentId);
                return false;
            }

            log.info("{}-{} saved at{}", this.collectionName, documentId, writeResult.getUpdateTime());
            return true;
        });
    }

    public void delete(T model) {
        String documentId = getDocumentId(model);
        ApiFuture<WriteResult> resultApiFuture = this.collectionReference.document(documentId).delete();
    }

    public void delete(String documentId) {
        ApiFuture<WriteResult> resultApiFuture = this.collectionReference.document(documentId).delete();
    }

    public CompletableFuture<List<T>> retrieveAll() {
        ApiFuture<QuerySnapshot> querySnapshotApiFuture = this.collectionReference.get();

        return this.toCompletableFuture(querySnapshotApiFuture).thenApplyAsync(querySnapshot -> {
            List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshot.getDocuments();

            return queryDocumentSnapshots.stream()
                    .map(queryDocumentSnapshot -> queryDocumentSnapshot.toObject(this.parameterizedType))
                    .collect(Collectors.toList());
        });
    }


    public CompletableFuture<Optional<T>> get(String documentId) {
        DocumentReference documentReference = this.collectionReference.document(documentId);
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = documentReference.get();

        return this.toCompletableFuture(documentSnapshotApiFuture).thenApplyAsync(documentSnapshot -> {
            return documentSnapshot.exists() ? Optional.ofNullable(documentSnapshot.toObject(this.parameterizedType)) : Optional.empty();
        });
    }


    protected String getDocumentId(T t) {
        Object key;
        Class<?> clzz = t.getClass();
        do {
            key = getKeyFromFields(clzz, t);
            clzz = clzz.getSuperclass();
        } while (key == null && clzz != null);

        if (key == null) {
            return UUID.randomUUID().toString();
        }
        return String.valueOf(key);
    }

    private Object getKeyFromFields(Class<?> clazz, Object t) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(DocumentId.class))
                .findFirst()
                .map(field -> getValue(t, field))
                .orElse(null);
    }

    @Nullable
    private Object getValue(Object t, java.lang.reflect.Field field) {
        field.setAccessible(true);
        try {
            return field.get(t);
        } catch (IllegalAccessException e) {
            log.error("Error in getting documentId key", e);
        }
        return null;
    }

    protected  <V> CompletableFuture<V> toCompletableFuture(ApiFuture<V> apiFuture) {
        final CompletableFuture<V> cf = new CompletableFuture<>();
        ApiFutures.addCallback(apiFuture,
                new ApiFutureCallback<>() {
                    @Override
                    public void onFailure(Throwable t) {
                        cf.completeExceptionally(t);
                    }

                    @Override
                    public void onSuccess(V result) {
                        cf.complete(result);
                    }
                },
                MoreExecutors.directExecutor());
        return cf;
    }

}