package studio.urlique.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.SessionManagementFilter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(corsFilter(), SessionManagementFilter.class)
                .csrf().disable()
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/url/create", "/url/{id}/info").permitAll()
                            .requestMatchers("/admin/**").hasAuthority("ADMIN")
                            .anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauthResourceServer ->
                        oauthResourceServer.jwt(Customizer.withDefaults())
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                        .stream()
                        .flatMap(List::stream)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
        return converter;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter();
    }

}
