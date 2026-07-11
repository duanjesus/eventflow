package com.eventflow.consumer;

import com.eventflow.entity.ProcessedEvent;
import com.eventflow.event.DomainEvent;
import com.eventflow.metrics.EventMetrics;
import com.eventflow.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens on the dead-letter queue that {@code eventflow.events.queue} routes
 * to once a message exhausts every retry attempt, and records the terminal
 * outcome for the dashboard. This is the only place a {@code ProcessedEvent}
 * ever becomes {@code DEAD_LETTERED} — the main consumer only ever sees
 * PROCESSED/FAILED for a given delivery attempt.
 */
@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    private final ProcessedEventRepository repository;
    private final EventMetrics metrics;

    public DeadLetterConsumer(ProcessedEventRepository repository, EventMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    @RabbitListener(queues = "${eventflow.rabbitmq.dead-letter-queue}")
    @Transactional
    public void onMessage(DomainEvent event) {
        log.error("Event dead-lettered after exhausting retries: eventId={} type={}",
                event.eventId(), event.eventType());
        repository.findByEventId(event.eventId()).ifPresent(ProcessedEvent::markDeadLettered);
        metrics.recordDeadLettered();
    }
}
