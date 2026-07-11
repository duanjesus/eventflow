import { useDashboardStats } from "@/hooks/useDashboardStats";
import { useRecentEvents } from "@/hooks/useRecentEvents";
import { useLiveNotifications } from "@/hooks/useLiveNotifications";
import { StatsGrid } from "@/components/StatsGrid";
import { RecentEventsTable } from "@/components/RecentEventsTable";
import { LiveFeed } from "@/components/LiveFeed";
import { SimulateControls } from "@/components/SimulateControls";

export function DashboardPage() {
  const { data: stats, isLoading: statsLoading } = useDashboardStats();
  const { data: eventsPage } = useRecentEvents();
  const liveEvents = useLiveNotifications();

  return (
    <div className="min-h-screen bg-canvas">
      <header className="border-b border-panelborder bg-panel">
        <div className="mx-auto flex max-w-7xl flex-col gap-3 px-6 py-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded bg-accent/15 font-mono text-sm font-bold text-accent">
              PQ
            </div>
            <div>
              <h1 className="text-base font-semibold tracking-tight text-slate-100">PulseQueue</h1>
              <p className="text-[11px] text-muted">Notification Infrastructure — Producer → RabbitMQ → Consumers → Email / Push / WebSocket</p>
            </div>
          </div>
          <SimulateControls />
        </div>
      </header>

      <main className="mx-auto max-w-7xl space-y-6 p-6">
        {statsLoading || !stats ? (
          <p className="text-muted">Loading stats…</p>
        ) : (
          <StatsGrid stats={stats} />
        )}

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="lg:col-span-2">
            <h2 className="mb-2 text-[11px] font-medium uppercase tracking-wider text-muted">Recent events</h2>
            <RecentEventsTable events={eventsPage?.content ?? []} />
          </div>
          <LiveFeed events={liveEvents} />
        </div>
      </main>
    </div>
  );
}
