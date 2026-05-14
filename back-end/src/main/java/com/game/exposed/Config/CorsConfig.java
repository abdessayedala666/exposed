package com.game.exposed.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:4200",
    "http://192.168.1.8:4200",
    "http://172.24.112.1:4200"
));
configuration.setAllowedMethods(Arrays.asList("*"));
configuration.setAllowedHeaders(Arrays.asList("*"));
configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
