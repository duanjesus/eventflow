import type { ProcessedEventItem, ProcessedEventStatus } from "@/types/dashboard";

const statusClasses: Record<ProcessedEventStatus, string> = {
  RECEIVED: "bg-status-received/15 text-status-received",
  PROCESSED: "bg-status-processed/15 text-status-processed",
  FAILED: "bg-status-failed/15 text-status-failed",
  DEAD_LETTERED: "bg-status-deadlettered/15 text-status-deadlettered",
  DUPLICATE: "bg-status-duplicate/15 text-status-duplicate",
};

export function RecentEventsTable({ events }: { events: ProcessedEventItem[] }) {
  return (
    <div className="overflow-x-auto rounded border border-panelborder bg-panel">
      <table className="min-w-full divide-y divide-panelborder text-sm">
        <thead>
          <tr>
            <th className="px-4 py-2 text-left text-[11px] font-medium uppercase tracking-wider text-muted">Event</th>
            <th className="px-4 py-2 text-left text-[11px] font-medium uppercase tracking-wider text-muted">Source</th>
            <th className="px-4 py-2 text-left text-[11px] font-medium uppercase tracking-wider text-muted">Status</th>
            <th className="px-4 py-2 text-left text-[11px] font-medium uppercase tracking-wider text-muted">Retries</th>
            <th className="px-4 py-2 text-left text-[11px] font-medium uppercase tracking-wider text-muted">Received</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-panelborder">
          {events.map((event) => (
            <tr key={event.id} className="hover:bg-white/5">
              <td className="px-4 py-2 font-mono text-xs text-slate-200">{event.eventType}</td>
              <td className="px-4 py-2 text-slate-400">{event.sourceService}</td>
              <td className="px-4 py-2">
                <span className={`rounded px-2 py-0.5 text-[11px] font-medium ${statusClasses[event.status]}`}>
                  {event.status}
                </span>
              </td>
              <td className="px-4 py-2 font-mono text-slate-400">{event.retryCount}</td>
              <td className="px-4 py-2 font-mono text-xs text-muted">
                {new Date(event.receivedAt).toLocaleTimeString()}
              </td>
            </tr>
          ))}
          {events.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-6 text-center text-muted">
                No events yet — try "Simulate events" above.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
