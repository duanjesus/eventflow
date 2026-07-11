package com.pulsequeue.consumer;

import com.pulsequeue.event.DomainEvent;
import com.pulsequeue.service.EventProcessingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private final EventProcessingService processingService;

    public EventConsumer(EventProcessingService processingService) {
        this.processingService = processingService;
    }

    @RabbitListener(queues = "${pulsequeue.rabbitmq.queue}")
    public void onMessage(DomainEvent event) {
        processingService.process(event);
    }
}
