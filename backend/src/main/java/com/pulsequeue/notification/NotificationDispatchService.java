package com.pulsequeue.notification;

import com.pulsequeue.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final List<NotificationChannel> channels;

    public NotificationDispatchService(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public void dispatch(DomainEvent event) {
        if (Boolean.TRUE.equals(event.payload().get("simulateFailure"))) {
            throw new NotificationDeliveryException(
                    "Simulated delivery failure for eventId=" + event.eventId());
        }
        for (NotificationChannel channel : channels) {
            log.debug("Dispatching eventId={} via {}", event.eventId(), channel.getChannelName());
            channel.send(event);
        }
    }
}
