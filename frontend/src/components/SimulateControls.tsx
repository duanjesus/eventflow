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
    <div className="flex gap-3">
      <button
        onClick={() => simulate.mutate()}
        disabled={simulate.isPending}
        className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-50"
      >
        Simulate events
      </button>
      <button
        onClick={() => simulateFailure.mutate()}
        disabled={simulateFailure.isPending}
        className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-500 disabled:opacity-50"
      >
        Simulate failure (retry → DLQ)
      </button>
    </div>
  );
}
