package com.eventflow.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class EventMetrics {

    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter deadLetteredCounter;
    private final Counter duplicateCounter;
    private final Counter retriedCounter;
    private final Timer processingTimer;

    public EventMetrics(MeterRegistry registry) {
        this.processedCounter = Counter.builder("eventflow.events.processed")
                .description("Events successfully processed and dispatched")
                .register(registry);
        this.failedCounter = Counter.builder("eventflow.events.failed")
                .description("Processing attempts that failed (before or without exhausting retries)")
                .register(registry);
        this.deadLetteredCounter = Counter.builder("eventflow.events.deadlettered")
                .description("Events that exhausted all retries and landed on the dead-letter queue")
                .register(registry);
        this.duplicateCounter = Counter.builder("eventflow.events.duplicate")
                .description("Deliveries skipped because the eventId was already processed")
                .register(registry);
        this.retriedCounter = Counter.builder("eventflow.events.retried")
                .description("Processing attempts that were retries (attempt 2+)")
                .register(registry);
        this.processingTimer = Timer.builder("eventflow.events.processing.duration")
                .description("Time spent dispatching a single event to its notification channels")
                .register(registry);
    }

    public void recordProcessed(Duration duration) {
        processedCounter.increment();
        processingTimer.record(duration);
    }

    public void recordFailed() {
        failedCounter.increment();
    }

    public void recordRetried() {
        retriedCounter.increment();
    }

    public void recordDeadLettered() {
        deadLetteredCounter.increment();
    }

    public void recordDuplicate() {
        duplicateCounter.increment();
    }
}
