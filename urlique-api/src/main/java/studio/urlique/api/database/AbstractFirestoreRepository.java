package studio.urlique.api.database;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import studio.urlique.api.utils.FutureUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Abstract class for handling Firestore collections async.
 *
 * @param <T> document type of collection.
 */
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

    /**
     * Save data to Firebase collection.
     *
     * @param model data to save.
     * @return state of the save request.
     */
    public CompletableFuture<Boolean> save(T model) {
        String documentId = getDocumentId(model);
        ApiFuture<WriteResult> resultApiFuture = this.collectionReference.document(documentId).set(model);

        return FutureUtils.toCompletableFuture(resultApiFuture).thenApplyAsync(writeResult -> {
            if (writeResult == null) {
                log.error("Error saving {}={}", this.collectionName, documentId);
                return false;
            }

            log.info("{}-{} saved at{}", this.collectionName, documentId, writeResult.getUpdateTime());
            return true;
        });
    }

    /**
     * Delete data by model object.
     *
     * @param model object to delete.
     */
    public void delete(T model) {
        String documentId = getDocumentId(model);
        ApiFuture<WriteResult> resultApiFuture = this.collectionReference.document(documentId).delete();
    }

    /**
     * Delete data by document ID.
     *
     * @param documentId ID of document to delete.
     */
    public void delete(String documentId) {
        ApiFuture<WriteResult> resultApiFuture = this.collectionReference.document(documentId).delete();
    }

    /**
     * Retrieve all data of collection.
     * ATTENTION: Could cause memory overload on big collection.
     *
     * @return whole collection.
     */
    public CompletableFuture<List<T>> retrieveAll() {
        ApiFuture<QuerySnapshot> querySnapshotApiFuture = this.collectionReference.get();

        return FutureUtils.toCompletableFuture(querySnapshotApiFuture).thenApplyAsync(querySnapshot -> {
            List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshot.getDocuments();

            return queryDocumentSnapshots.stream()
                    .map(queryDocumentSnapshot -> queryDocumentSnapshot.toObject(this.parameterizedType))
                    .collect(Collectors.toList());
        });
    }


    /**
     * Get certain data by ID of document.
     *
     * @param documentId ID of the document to fetch.
     * @return {@link Optional} of fetched data.
     */
    public CompletableFuture<Optional<T>> get(String documentId) {
        DocumentReference documentReference = this.collectionReference.document(documentId);
        ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = documentReference.get();

        return FutureUtils.toCompletableFuture(documentSnapshotApiFuture).thenApplyAsync(documentSnapshot -> {
            return documentSnapshot.exists() ? Optional.ofNullable(documentSnapshot.toObject(this.parameterizedType)) : Optional.empty();
        });
    }


    /**
     * Get document ID of data based on {@link DocumentId} annotation.
     *
     * @param t data to scan on for document ID.
     * @return ID of document.
     */
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

}