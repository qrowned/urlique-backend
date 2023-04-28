package studio.urlique.server.url.transformer;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import studio.urlique.api.url.UrlData;
import studio.urlique.api.database.DataTransformer;

import java.sql.ResultSet;

public final class UrlDataTransformer implements DataTransformer<UrlData> {

    @Override
    @SneakyThrows
    public UrlData transform(@NotNull ResultSet resultSet) {
        return new UrlData(
                resultSet.getString("id"),
                resultSet.getString("url"),
                resultSet.getTimestamp("createdAt").toInstant(),
                resultSet.getString("creator")
        );
    }

}
