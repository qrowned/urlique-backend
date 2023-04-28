package studio.urlique.api.database;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AsyncDataSource {

    @SneakyThrows
    PreparedStatement prepare(@NotNull String statement);

    void update(@NotNull PreparedStatement preparedStatement);

    CompletableFuture<ResultSet> query(@NotNull PreparedStatement preparedStatement);

    <T> CompletableFuture<T> query(@NotNull PreparedStatement preparedStatement, @NotNull DataTransformer<T> dataTransformer);

    <T> CompletableFuture<List<T>> queryAll(@NotNull PreparedStatement preparedStatement, @NotNull DataTransformer<T> dataTransformer);

    <T> T execute(ConnectionCallback<T> callback);

    interface ConnectionCallback<T> {
        @SneakyThrows
        T doInConnection(Connection conn) throws SQLException;
    }

}
