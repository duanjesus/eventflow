package com.eventflow.notification;

import com.eventflow.event.DomainEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NotificationDispatchServiceTest {

    @Test
    void dispatchesToEveryRegisteredChannel() {
        NotificationChannel email = mock(NotificationChannel.class);
        NotificationChannel push = mock(NotificationChannel.class);
        NotificationDispatchService service = new NotificationDispatchService(List.of(email, push));
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("amount", 10));

        service.dispatch(event);

        verify(email).send(event);
        verify(push).send(event);
    }

    @Test
    void simulateFailureFlagThrowsWithoutCallingAnyChannel() {
        NotificationChannel email = mock(NotificationChannel.class);
        NotificationDispatchService service = new NotificationDispatchService(List.of(email));
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("simulateFailure", true));

        assertThrows(NotificationDeliveryException.class, () -> service.dispatch(event));

        verify(email, never()).send(any());
    }
}
