package studio.urlique.server;

import dev.qrowned.config.CommonConfigService;
import dev.qrowned.config.api.ConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public final class UrliqueServerBeans {

    private static final ConfigService CONFIG_SERVICE = new CommonConfigService("./configs/");

    @Bean
    public ConfigService configService() {
        return CONFIG_SERVICE;
    }

}
