import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type { DomainEventMessage } from "@/types/dashboard";

const MAX_FEED_SIZE = 30;

export function useLiveNotifications() {
  const [events, setEvents] = useState<DomainEventMessage[]>([]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe("/topic/notifications", (message) => {
          const event = JSON.parse(message.body) as DomainEventMessage;
          setEvents((prev) => [event, ...prev].slice(0, MAX_FEED_SIZE));
        });
      },
    });
    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return events;
}
