package com.eventflow.notification;

import com.eventflow.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulated channel — logs what a real push provider (FCM/APNs) integration
 * would send, rather than calling one.
 */
@Component
public class PushNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationChannel.class);

    @Override
    public String getChannelName() {
        return "PUSH";
    }

    @Override
    public void send(DomainEvent event) {
        log.info("[Push] Notified about {} from {} (eventId={})",
                event.eventType(), event.sourceService(), event.eventId());
    }
}
