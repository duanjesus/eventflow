import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";

export function SimulateControls() {
  const queryClient = useQueryClient();

  const simulate = useMutation({
    mutationFn: () => api.post("/events/simulate"),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["dashboard-stats"] }),
  });

  const simulateFailure = useMutation({
    mutationFn: () =>
      api.post("/events", {
        eventType: "expense.created",
        sourceService: "cashpilot",
        payload: { simulateFailure: true, description: "Deliberately broken demo event" },
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["dashboard-stats"] }),
  });

  return (
    <div className="flex gap-2">
      <button
        onClick={() => simulate.mutate()}
        disabled={simulate.isPending}
        className="rounded border border-accent/40 bg-accent/10 px-3 py-1.5 text-xs font-medium text-accent hover:bg-accent/20 disabled:opacity-50"
      >
        Simulate events
      </button>
      <button
        onClick={() => simulateFailure.mutate()}
        disabled={simulateFailure.isPending}
        className="rounded border border-status-deadlettered/40 bg-status-deadlettered/10 px-3 py-1.5 text-xs font-medium text-status-deadlettered hover:bg-status-deadlettered/20 disabled:opacity-50"
      >
        Simulate failure (retry → DLQ)
      </button>
    </div>
  );
}
