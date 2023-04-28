package studio.urlique.server;

import dev.qrowned.config.CommonConfigService;
import dev.qrowned.config.api.ConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import studio.urlique.server.config.MysqlConfig;

@Component
public final class UrliqueServerBeans {

    private static final ConfigService CONFIG_SERVICE = new CommonConfigService("./configs/");

    private final MysqlConfig mysqlConfig = CONFIG_SERVICE.registerConfig("mysql.json", MysqlConfig.class);

    @Bean
    public ConfigService configService() {
        return CONFIG_SERVICE;
    }

    @Bean
    public MysqlConfig mysqlConfig() {
        return this.mysqlConfig;
    }

}
