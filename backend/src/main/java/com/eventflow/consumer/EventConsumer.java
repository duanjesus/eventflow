package com.eventflow.consumer;

import com.eventflow.event.DomainEvent;
import com.eventflow.service.EventProcessingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private final EventProcessingService processingService;

    public EventConsumer(EventProcessingService processingService) {
        this.processingService = processingService;
    }

    @RabbitListener(queues = "${eventflow.rabbitmq.queue}")
    public void onMessage(DomainEvent event) {
        processingService.process(event);
    }
}
