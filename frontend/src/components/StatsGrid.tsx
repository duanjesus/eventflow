import { StatCard } from "@/components/StatCard";
import type { DashboardStats } from "@/types/dashboard";

export function StatsGrid({ stats }: { stats: DashboardStats }) {
  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-7">
      <StatCard label="Queue depth" value={stats.queueDepth} />
      <StatCard label="Received" value={stats.totalReceived} />
      <StatCard label="Processed" value={stats.processed} />
      <StatCard label="Failed" value={stats.failed} tone="warning" />
      <StatCard label="Dead-lettered" value={stats.deadLettered} tone="danger" />
      <StatCard label="Duplicates skipped" value={stats.duplicate} />
      <StatCard label="Total retries" value={stats.totalRetries} tone="warning" />
    </div>
  );
}
