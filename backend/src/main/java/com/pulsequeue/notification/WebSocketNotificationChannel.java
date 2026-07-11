package com.pulsequeue.notification;

import com.pulsequeue.event.DomainEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * The one real (non-simulated) channel: broadcasts the event to every
 * dashboard client subscribed to {@code /topic/notifications} over STOMP.
 */
@Component
public class WebSocketNotificationChannel implements NotificationChannel {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationChannel(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public String getChannelName() {
        return "WEBSOCKET";
    }

    @Override
    public void send(DomainEvent event) {
        messagingTemplate.convertAndSend("/topic/notifications", event);
    }
}
