import type { DomainEventMessage } from "@/types/dashboard";

export function LiveFeed({ events }: { events: DomainEventMessage[] }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <h2 className="mb-2 text-sm font-medium text-slate-500">Live notification feed (WebSocket)</h2>
      <ul className="max-h-80 space-y-1 overflow-y-auto text-sm">
        {events.map((event, index) => (
          <li
            key={`${event.eventId}-${index}`}
            className="rounded bg-slate-50 px-2 py-1 font-mono text-xs text-slate-700"
          >
            {new Date(event.occurredAt).toLocaleTimeString()} — {event.eventType} from {event.sourceService}
          </li>
        ))}
        {events.length === 0 && <li className="text-slate-400">Waiting for events…</li>}
      </ul>
    </div>
  );
}
