package com.pulsequeue.producer;

import com.pulsequeue.config.RabbitMqProperties;
import com.pulsequeue.event.DomainEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final AmqpTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    public EventPublisher(AmqpTemplate rabbitTemplate, RabbitMqProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publish(DomainEvent event) {
        rabbitTemplate.convertAndSend(properties.exchange(), event.eventType(), event);
    }
}
