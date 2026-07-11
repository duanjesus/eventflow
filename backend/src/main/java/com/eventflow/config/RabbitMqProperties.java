package com.eventflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventflow.rabbitmq")
public record RabbitMqProperties(
        String exchange,
        String queue,
        String deadLetterExchange,
        String deadLetterQueue,
        String routingKeyPattern
) {
}
