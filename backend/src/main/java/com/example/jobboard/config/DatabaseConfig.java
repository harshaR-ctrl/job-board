package com.example.jobboard.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties props = new DataSourceProperties();

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
                props.setUrl(jdbcUrl);

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":");
                    props.setUsername(parts[0]);
                    props.setPassword(parts[1]);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
            }
        }

        return props;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}