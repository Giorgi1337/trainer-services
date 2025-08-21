package com.gym.training.config;

import feign.RequestInterceptor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Configuration
public class FeignConfig {

    private static final String SECRET = "my-very-secure-secret-key-for-jwt-1234567890"; // Shared secret
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // Propagate transactionId
            String txId = MDC.get("transactionId");
            if (txId != null) {
                template.header("transactionId", txId);
            }

            // Generate and add JWT
            String jwt = Jwts.builder()
                    .issuer("training-service")
                    .subject("integration")
                    .issuedAt(new Date())
                    // .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour; add for production
                    .signWith(key)
                    .compact();
            template.header("Authorization", "Bearer " + jwt);
        };
    }
}