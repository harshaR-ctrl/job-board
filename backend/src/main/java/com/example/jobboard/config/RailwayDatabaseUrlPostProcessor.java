package com.example.jobboard.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RailwayDatabaseUrlPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            try {
                URI uri = new URI(databaseUrl);
                String userInfo = uri.getUserInfo();
                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath();
                String dbName = path.startsWith("/") ? path.substring(1) : path;

                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);

                Map<String, Object> props = new HashMap<>();
                props.put("spring.datasource.url", jdbcUrl);

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":");
                    props.put("spring.datasource.username", parts[0]);
                    props.put("spring.datasource.password", parts[1]);
                }

                environment.getPropertySources().addFirst(new MapPropertySource("railway-db", props));
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
            }
        }
    }
}