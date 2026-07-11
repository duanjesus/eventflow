interface StatCardProps {
  label: string;
  value: number;
  tone?: "default" | "danger" | "warning";
}

const toneClasses: Record<NonNullable<StatCardProps["tone"]>, string> = {
  default: "text-slate-900",
  danger: "text-red-600",
  warning: "text-amber-600",
};

export function StatCard({ label, value, tone = "default" }: StatCardProps) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-sm font-medium text-slate-500">{label}</p>
      <p className={`mt-1 text-3xl font-semibold ${toneClasses[tone]}`}>{value.toLocaleString()}</p>
    </div>
  );
}
