interface StatCardProps {
  label: string;
  value: number | string;
  tone?: "default" | "danger" | "warning" | "success" | "accent";
  isStatus?: boolean;
}

const toneClasses: Record<NonNullable<StatCardProps["tone"]>, string> = {
  default: "text-slate-100",
  accent: "text-accent",
  success: "text-status-processed",
  warning: "text-status-failed",
  danger: "text-status-deadlettered",
};

const dotClasses: Record<NonNullable<StatCardProps["tone"]>, string> = {
  default: "bg-slate-500",
  accent: "bg-accent",
  success: "bg-status-processed",
  warning: "bg-status-failed",
  danger: "bg-status-deadlettered",
};

export function StatCard({ label, value, tone = "default", isStatus = false }: StatCardProps) {
  return (
    <div className="rounded border border-panelborder bg-panel p-4">
      <p className="text-[11px] font-medium uppercase tracking-wider text-muted">{label}</p>
      {isStatus ? (
        <div className="mt-2 flex items-center gap-2">
          <span className={`h-2.5 w-2.5 rounded-full ${dotClasses[tone]} ${tone !== "danger" ? "animate-pulse" : ""}`} />
          <span className={`font-mono text-2xl font-semibold ${toneClasses[tone]}`}>{value}</span>
        </div>
      ) : (
        <p className={`mt-1 font-mono text-3xl font-semibold tabular-nums ${toneClasses[tone]}`}>
          {typeof value === "number" ? value.toLocaleString() : value}
        </p>
      )}
    </div>
  );
}
