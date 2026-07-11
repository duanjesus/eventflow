import type { DomainEventMessage } from "@/types/dashboard";

export function LiveFeed({ events }: { events: DomainEventMessage[] }) {
  return (
    <div className="rounded border border-panelborder bg-panel p-4">
      <h2 className="mb-2 flex items-center gap-2 text-[11px] font-medium uppercase tracking-wider text-muted">
        <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-accent" />
        Live feed (WebSocket)
      </h2>
      <ul className="max-h-80 space-y-1 overflow-y-auto text-sm">
        {events.map((event, index) => (
          <li
            key={`${event.eventId}-${index}`}
            className="rounded border border-panelborder/60 bg-black/20 px-2 py-1 font-mono text-xs text-slate-300"
          >
            <span className="text-muted">{new Date(event.occurredAt).toLocaleTimeString()}</span>{" "}
            <span className="text-accent">{event.eventType}</span> from {event.sourceService}
          </li>
        ))}
        {events.length === 0 && <li className="text-muted">Waiting for events…</li>}
      </ul>
    </div>
  );
}
