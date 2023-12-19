package com.api.notebook.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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

    @Bean
    public Queue queue() {
        return new Queue(rabbitMQQueue);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(rabbitMQExchange);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(topicExchange())
                .with(rabbitMQRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        var rabbitMQTemplate = new RabbitTemplate(connectionFactory);
        rabbitMQTemplate.setMessageConverter(messageConverter());
        return rabbitMQTemplate;
    }

}
