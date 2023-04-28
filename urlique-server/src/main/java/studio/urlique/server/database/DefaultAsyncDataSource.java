package studio.urlique.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import studio.urlique.api.database.AsyncDataSource;
import studio.urlique.api.database.DataTransformer;
import studio.urlique.server.config.MysqlConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public final class DefaultAsyncDataSource implements AsyncDataSource {

    private final DataSource dataSource;

    @SneakyThrows
    public DefaultAsyncDataSource(@NotNull MysqlConfig mysqlConfig) {
        Class.forName("com.mysql.cj.jdbc.Driver");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + mysqlConfig.getHostname() + ":" + mysqlConfig.getPort() + "/" + mysqlConfig.getDatabase());
        config.setUsername(mysqlConfig.getUsername());
        config.setPassword(mysqlConfig.getPassword());
        config.setMinimumIdle(mysqlConfig.getMinIdle());
        config.setMaximumPoolSize(mysqlConfig.getMaxPoolSize());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("allowMultiQueries", "true");
        this.dataSource = new HikariDataSource(config);
    }

    @Override
    @SneakyThrows
    public PreparedStatement prepare(@NotNull String statement) {
        return this.dataSource.getConnection().prepareStatement(statement);
    }

    @Override
    public void update(@NotNull PreparedStatement preparedStatement) {
        CompletableFuture.runAsync(() -> {
            try {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    preparedStatement.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public CompletableFuture<ResultSet> query(@NotNull PreparedStatement preparedStatement) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return preparedStatement.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public <T> CompletableFuture<T> query(@NotNull PreparedStatement preparedStatement, @NotNull DataTransformer<T> dataTransformer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) return dataTransformer.transform(resultSet);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    preparedStatement.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    @Override
    public <T> CompletableFuture<List<T>> queryAll(@NotNull PreparedStatement preparedStatement, @NotNull DataTransformer<T> dataTransformer) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> list = new ArrayList<>();
            try {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                    list.add(dataTransformer.transform(resultSet));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    preparedStatement.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return list;
        });
    }

    @Override
    public <T> T execute(AsyncDataSource.ConnectionCallback<T> callback) {
        try (Connection conn = this.dataSource.getConnection()) {
            return callback.doInConnection(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Error during execution.", e);
        }
    }

}
