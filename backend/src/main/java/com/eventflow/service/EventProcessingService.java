package com.eventflow.service;

import com.eventflow.entity.ProcessedEvent;
import com.eventflow.event.DomainEvent;
import com.eventflow.metrics.EventMetrics;
import com.eventflow.notification.NotificationDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class EventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);

    private final ProcessedEventRecorder recorder;
    private final NotificationDispatchService dispatchService;
    private final DeduplicationService deduplicationService;
    private final EventMetrics metrics;

    public EventProcessingService(ProcessedEventRecorder recorder,
                                   NotificationDispatchService dispatchService,
                                   DeduplicationService deduplicationService,
                                   EventMetrics metrics) {
        this.recorder = recorder;
        this.dispatchService = dispatchService;
        this.deduplicationService = deduplicationService;
        this.metrics = metrics;
    }

    public void process(DomainEvent event) {
        ProcessedEvent record = recorder.findOrCreate(event);

        if (deduplicationService.isDuplicate(event.eventId())) {
            recorder.recordDuplicate(record);
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
            recorder.recordProcessed(record);
            deduplicationService.markProcessed(event.eventId());
            metrics.recordProcessed(Duration.between(startedAt, Instant.now()));
            log.info("Processed eventId={} type={}", event.eventId(), event.eventType());
        } catch (RuntimeException ex) {
            recorder.recordFailed(record, ex.getMessage());
            metrics.recordFailed();
            log.warn("Failed to process eventId={} (attempt {}): {}",
                    event.eventId(), record.getRetryCount(), ex.getMessage());
            throw ex;
        }
    }
}
