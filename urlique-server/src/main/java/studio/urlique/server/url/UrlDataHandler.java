package studio.urlique.server.url;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import studio.urlique.api.database.AsyncDataSource;
import studio.urlique.api.url.UrlData;
import studio.urlique.server.url.transformer.UrlDataTransformer;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public final class UrlDataHandler {

    private static final UrlDataTransformer DATA_TRANSFORMER = new UrlDataTransformer();

    private static final String FETCH_STATEMENT = "select * from url_data where id = ?;";
    private static final String FETCH_ALL_STATEMENT = "select * from url_data;";

    private static final String INSERT_STATEMENT = "insert into url_data(id, url, creator) values (?, ?, ?);";
    private static final String DELETE_STATEMENT = "delete from url_data where id = ?;";

    private final AsyncDataSource dataSource;
    private final AsyncLoadingCache<String, UrlData> accountAsyncLoadingCache;

    public UrlDataHandler(@NotNull AsyncDataSource dataSource) {
        this.dataSource = dataSource;
        this.accountAsyncLoadingCache = Caffeine.newBuilder()
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .buildAsync((id, executor) -> {
                    PreparedStatement preparedStatement = dataSource.prepare(FETCH_STATEMENT);
                    preparedStatement.setString(1, id);
                    return dataSource.query(preparedStatement, DATA_TRANSFORMER);
                });
    }

    public CompletableFuture<UrlData> fetchUrlData(@NotNull String id) {
        return this.accountAsyncLoadingCache.get(id);
    }

    public CompletableFuture<List<UrlData>> fetchAllUrlData(@NotNull String id) {
        PreparedStatement preparedStatement = this.dataSource.prepare(FETCH_ALL_STATEMENT);
        return this.dataSource.queryAll(preparedStatement, DATA_TRANSFORMER);
    }

    @SneakyThrows
    public void insertData(@NotNull UrlData urlData) {
        PreparedStatement preparedStatement = this.dataSource.prepare(INSERT_STATEMENT);
        preparedStatement.setString(1, urlData.id());
        preparedStatement.setString(2, urlData.url());
        preparedStatement.setString(3, urlData.creator());
        this.dataSource.update(preparedStatement);
    }

    @SneakyThrows
    public void deleteData(@NotNull String id) {
        PreparedStatement preparedStatement = this.dataSource.prepare(DELETE_STATEMENT);
        preparedStatement.setString(1, id);
        this.dataSource.update(preparedStatement);
    }

    public void invalidate(@NotNull String id) {
        this.accountAsyncLoadingCache.synchronous().invalidate(id);
    }

    public void invalidateAll() {
        this.accountAsyncLoadingCache.synchronous().invalidateAll();
    }

}