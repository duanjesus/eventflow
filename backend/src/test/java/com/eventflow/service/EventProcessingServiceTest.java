package com.eventflow.service;

import com.eventflow.entity.ProcessedEvent;
import com.eventflow.entity.ProcessedEventStatus;
import com.eventflow.event.DomainEvent;
import com.eventflow.metrics.EventMetrics;
import com.eventflow.notification.NotificationDeliveryException;
import com.eventflow.notification.NotificationDispatchService;
import com.eventflow.repository.ProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventProcessingServiceTest {

    @Mock
    private ProcessedEventRepository repository;
    @Mock
    private NotificationDispatchService dispatchService;
    @Mock
    private DeduplicationService deduplicationService;
    @Mock
    private EventMetrics metrics;

    private EventProcessingService service;

    @BeforeEach
    void setUp() {
        service = new EventProcessingService(repository, dispatchService, deduplicationService, metrics, new ObjectMapper());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void successfulProcessingMarksRecordProcessedAndClaimsDedupKey() {
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("amount", 10));
        when(repository.findByEventId(event.eventId())).thenReturn(Optional.empty());
        when(deduplicationService.isDuplicate(event.eventId())).thenReturn(false);

        service.process(event);

        verify(dispatchService).dispatch(event);
        verify(deduplicationService).markProcessed(event.eventId());
        verify(metrics).recordProcessed(any(Duration.class));

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(repository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertEquals(ProcessedEventStatus.PROCESSED, captor.getValue().getStatus());
    }

    @Test
    void duplicateEventSkipsDispatchAndMarksDuplicate() {
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("amount", 10));
        when(repository.findByEventId(event.eventId())).thenReturn(Optional.empty());
        when(deduplicationService.isDuplicate(event.eventId())).thenReturn(true);

        service.process(event);

        verify(dispatchService, never()).dispatch(any());
        verify(metrics).recordDuplicate();
        verify(deduplicationService, never()).markProcessed(any());
    }

    @Test
    void failedDispatchMarksFailedAndRethrows() {
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("simulateFailure", true));
        when(repository.findByEventId(event.eventId())).thenReturn(Optional.empty());
        when(deduplicationService.isDuplicate(event.eventId())).thenReturn(false);
        doThrow(new NotificationDeliveryException("boom")).when(dispatchService).dispatch(event);

        assertThrows(NotificationDeliveryException.class, () -> service.process(event));

        verify(metrics).recordFailed();
        verify(deduplicationService, never()).markProcessed(any());
    }

    @Test
    void reprocessingAnAlreadyFailedRecordRecordsARetryMetric() {
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("amount", 10));
        ProcessedEvent existing = ProcessedEvent.received(event.eventId(), event.eventType(), event.sourceService(), "{}");
        existing.markFailed("previous attempt failed");
        when(repository.findByEventId(event.eventId())).thenReturn(Optional.of(existing));
        when(deduplicationService.isDuplicate(event.eventId())).thenReturn(false);

        service.process(event);

        verify(metrics).recordRetried();
        assertEquals(ProcessedEventStatus.PROCESSED, existing.getStatus());
    }
}
