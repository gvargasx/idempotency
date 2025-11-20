package org.example.idempotency;

import org.example.idempotency.config.IdempotencyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdempotencyApiApplication.class, args);
    }
}
