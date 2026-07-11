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
    <div className="mx-auto max-w-6xl space-y-6 p-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">EventFlow Dashboard</h1>
          <p className="text-sm text-slate-500">Producer → RabbitMQ → Consumer → Email / Push / WebSocket</p>
        </div>
        <SimulateControls />
      </header>

      {statsLoading || !stats ? (
        <p className="text-slate-400">Loading stats…</p>
      ) : (
        <StatsGrid stats={stats} />
      )}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <h2 className="mb-2 text-sm font-medium text-slate-500">Recent events</h2>
          <RecentEventsTable events={eventsPage?.content ?? []} />
        </div>
        <LiveFeed events={liveEvents} />
      </div>
    </div>
  );
}
