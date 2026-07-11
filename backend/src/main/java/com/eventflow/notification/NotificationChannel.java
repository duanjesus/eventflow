package com.eventflow.notification;

import com.eventflow.event.DomainEvent;

public interface NotificationChannel {

    String getChannelName();

    void send(DomainEvent event);
}
