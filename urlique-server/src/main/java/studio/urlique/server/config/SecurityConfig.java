package studio.urlique.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> {
                    requests
                            .requestMatchers("/admin/**").permitAll()
                            .anyRequest().authenticated();
                })
                .httpBasic()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }

}
