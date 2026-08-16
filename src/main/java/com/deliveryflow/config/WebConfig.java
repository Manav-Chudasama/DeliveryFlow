package com.deliveryflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS for local development.
 *
 * <p>In normal use the Vite dev server proxies {@code /api} to this application, so the
 * browser sees a single origin and CORS never comes into play. This exists so that calling
 * the API directly on :8080 from a browser tab — or running the frontend without the proxy —
 * also works.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
