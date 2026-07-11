import { StatCard } from "@/components/StatCard";
import type { DashboardStats } from "@/types/dashboard";

export function StatsGrid({ stats }: { stats: DashboardStats }) {
  const rabbitUp = stats.rabbitMqStatus === "UP";

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <StatCard label="Messages Published" value={stats.totalReceived} tone="accent" />
        <StatCard label="Messages Processed" value={stats.processed} tone="success" />
        <StatCard label="Dead Letter Queue" value={stats.deadLettered} tone="danger" />
        <StatCard label="Retry Queue" value={stats.totalRetries} tone="warning" />
        <StatCard label="Consumers" value={stats.consumerCount} tone="default" />
        <StatCard
          label="RabbitMQ Status"
          value={stats.rabbitMqStatus}
          tone={rabbitUp ? "success" : "danger"}
          isStatus
        />
      </div>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <StatCard label="Queue Depth" value={stats.queueDepth} />
        <StatCard label="Failed Attempts" value={stats.failed} tone="warning" />
        <StatCard label="Duplicates Skipped" value={stats.duplicate} />
      </div>
    </div>
  );
}
