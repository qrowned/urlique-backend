package studio.urlique.server.config;

import dev.qrowned.config.api.ConfigAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
public final class MysqlConfig implements ConfigAdapter<MysqlConfig> {

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    private int minIdle;
    private int maxPoolSize;

    @Override
    public void reload(@NotNull MysqlConfig config) {
        this.hostname = config.getHostname();
        this.port = config.getPort();
        this.database = config.getDatabase();
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.minIdle = config.getMinIdle();
        this.maxPoolSize = config.getMaxPoolSize();
    }

}
