package studio.urlique.api.utils;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.CompletableFuture;

public class FutureUtils {

    public static <V> CompletableFuture<V> toCompletableFuture(ApiFuture<V> apiFuture) {
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
