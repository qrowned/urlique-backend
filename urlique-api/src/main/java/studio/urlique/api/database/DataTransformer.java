package studio.urlique.api.database;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;

public interface DataTransformer<T> {

    T transform(@NotNull ResultSet resultSet);

}
