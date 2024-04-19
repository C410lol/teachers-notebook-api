package com.api.notebook.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Random;

@Configuration
public class ApplicationConfig {

    @Value("${spring.rabbitmq.queue}")
    private String rabbitMQQueue;

    @Value("${spring.rabbitmq.exchange}")
    private String rabbitMQExchange;

    @Value("${spring.rabbitmq.routing-key}")
    private String rabbitMQRoutingKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Random random() {
        return new Random();
    }


}
