package com.eventflow.service;

import com.eventflow.entity.ProcessedEvent;
import com.eventflow.event.DomainEvent;
import com.eventflow.metrics.EventMetrics;
import com.eventflow.notification.NotificationDispatchService;
import com.eventflow.repository.ProcessedEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class EventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);

    private final ProcessedEventRepository repository;
    private final NotificationDispatchService dispatchService;
    private final DeduplicationService deduplicationService;
    private final EventMetrics metrics;
    private final ObjectMapper objectMapper;

    public EventProcessingService(ProcessedEventRepository repository,
                                   NotificationDispatchService dispatchService,
                                   DeduplicationService deduplicationService,
                                   EventMetrics metrics,
                                   ObjectMapper objectMapper) {
        this.repository = repository;
        this.dispatchService = dispatchService;
        this.deduplicationService = deduplicationService;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void process(DomainEvent event) {
        ProcessedEvent record = repository.findByEventId(event.eventId())
                .orElseGet(() -> repository.save(newRecord(event)));

        if (deduplicationService.isDuplicate(event.eventId())) {
            record.markDuplicate();
            repository.save(record);
            metrics.recordDuplicate();
            log.info("Skipped duplicate eventId={} type={}", event.eventId(), event.eventType());
            return;
        }

        if (record.getRetryCount() > 0) {
            metrics.recordRetried();
        }

        Instant startedAt = Instant.now();
        try {
            dispatchService.dispatch(event);
            record.markProcessed();
            deduplicationService.markProcessed(event.eventId());
            metrics.recordProcessed(Duration.between(startedAt, Instant.now()));
            log.info("Processed eventId={} type={}", event.eventId(), event.eventType());
        } catch (RuntimeException ex) {
            record.markFailed(ex.getMessage());
            repository.save(record);
            metrics.recordFailed();
            log.warn("Failed to process eventId={} (attempt {}): {}",
                    event.eventId(), record.getRetryCount(), ex.getMessage());
            throw ex;
        }
        repository.save(record);
    }

    private ProcessedEvent newRecord(DomainEvent event) {
        return ProcessedEvent.received(event.eventId(), event.eventType(), event.sourceService(), toJson(event.payload()));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }
}
