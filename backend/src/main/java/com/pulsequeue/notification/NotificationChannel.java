package com.pulsequeue.notification;

import com.pulsequeue.event.DomainEvent;

public interface NotificationChannel {

    String getChannelName();

    void send(DomainEvent event);
}
