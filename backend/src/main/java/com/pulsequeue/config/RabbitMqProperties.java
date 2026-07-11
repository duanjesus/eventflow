package com.pulsequeue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsequeue.rabbitmq")
public record RabbitMqProperties(
        String exchange,
        String queue,
        String deadLetterExchange,
        String deadLetterQueue,
        String routingKeyPattern
) {
}
