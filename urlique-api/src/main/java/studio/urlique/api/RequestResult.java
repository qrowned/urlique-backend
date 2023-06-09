package studio.urlique.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Represents a Result provided by the API/Server.
 *
 * @param <T> the type of the result body.
 */
@Getter
@AllArgsConstructor
public final class RequestResult<T> implements Serializable {

    private final T result;
    private final boolean success;
    private final String message;

    private RequestResult(@NotNull T result) {
        this.result = result;
        this.success = true;
        this.message = null;
    }

    private RequestResult(@NotNull String message) {
        this.result = null;
        this.success = false;
        this.message = message;
    }

    /**
     * Create a successful result with body.
     *
     * @param result body of result.
     * @param <T>    type of the result.
     * @return result object of body.
     */
    public static <T> RequestResult<T> ok(@NotNull T result) {
        return new RequestResult<>(result);
    }

    /**
     * Create a failed result with message.
     *
     * @param message error message.
     * @param <T>     type of the result.
     * @return result object of error message.
     */
    public static <T> RequestResult<T> error(@NotNull String message) {
        return new RequestResult<>(message);
    }

}
