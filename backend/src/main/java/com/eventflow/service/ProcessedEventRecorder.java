package com.eventflow.service;

import com.eventflow.entity.ProcessedEvent;
import com.eventflow.event.DomainEvent;
import com.eventflow.repository.ProcessedEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Each method here is its own transaction, committed independently the
 * moment it returns. This matters specifically for {@link #recordFailed}:
 * {@link EventProcessingService#process} deliberately lets the triggering
 * exception propagate afterwards (so Spring AMQP's retry interceptor sees
 * it), and a plain {@code @Transactional} on {@code process()} itself would
 * roll back everything written during that call — including the "this
 * attempt failed" row — the instant the exception left the method. Calling
 * through a separate Spring-proxied bean sidesteps that entirely, since each
 * call here is its own independently-committed transaction.
 */
@Service
public class ProcessedEventRecorder {

    private final ProcessedEventRepository repository;
    private final ObjectMapper objectMapper;

    public ProcessedEventRecorder(ProcessedEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProcessedEvent findOrCreate(DomainEvent event) {
        return repository.findByEventId(event.eventId())
                .orElseGet(() -> repository.save(ProcessedEvent.received(
                        event.eventId(), event.eventType(), event.sourceService(), toJson(event.payload()))));
    }

    @Transactional
    public void recordProcessed(ProcessedEvent record) {
        record.markProcessed();
        repository.save(record);
    }

    @Transactional
    public void recordFailed(ProcessedEvent record, String errorMessage) {
        record.markFailed(errorMessage);
        repository.save(record);
    }

    @Transactional
    public void recordDuplicate(ProcessedEvent record) {
        record.markDuplicate();
        repository.save(record);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }
}
