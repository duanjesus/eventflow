import type { ProcessedEventItem, ProcessedEventStatus } from "@/types/dashboard";

const statusClasses: Record<ProcessedEventStatus, string> = {
  RECEIVED: "bg-blue-100 text-blue-800",
  PROCESSED: "bg-green-100 text-green-800",
  FAILED: "bg-amber-100 text-amber-800",
  DEAD_LETTERED: "bg-red-100 text-red-800",
  DUPLICATE: "bg-slate-100 text-slate-800",
};

export function RecentEventsTable({ events }: { events: ProcessedEventItem[] }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white shadow-sm">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-4 py-2 text-left font-medium text-slate-500">Event</th>
            <th className="px-4 py-2 text-left font-medium text-slate-500">Source</th>
            <th className="px-4 py-2 text-left font-medium text-slate-500">Status</th>
            <th className="px-4 py-2 text-left font-medium text-slate-500">Retries</th>
            <th className="px-4 py-2 text-left font-medium text-slate-500">Received</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {events.map((event) => (
            <tr key={event.id}>
              <td className="px-4 py-2 font-mono text-xs text-slate-700">{event.eventType}</td>
              <td className="px-4 py-2 text-slate-600">{event.sourceService}</td>
              <td className="px-4 py-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${statusClasses[event.status]}`}>
                  {event.status}
                </span>
              </td>
              <td className="px-4 py-2 text-slate-600">{event.retryCount}</td>
              <td className="px-4 py-2 text-slate-500">{new Date(event.receivedAt).toLocaleTimeString()}</td>
            </tr>
          ))}
          {events.length === 0 && (
            <tr>
              <td colSpan={5} className="px-4 py-6 text-center text-slate-400">
                No events yet — try "Simulate events" above.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
