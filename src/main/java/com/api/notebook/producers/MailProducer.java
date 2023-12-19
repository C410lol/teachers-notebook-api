package com.api.notebook.producers;

import com.api.notebook.models.EmailModel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailProducer {

    @Value("${spring.rabbitmq.exchange}")
    private String rabbitMQExchange;

    @Value("${spring.rabbitmq.routing-key}")
    private String rabbitMQRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public void sendMailMessage(@NotNull EmailModel emailModel) {
        rabbitTemplate.convertAndSend(rabbitMQExchange, rabbitMQRoutingKey, emailModel);
    }

}
