import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { PageResponse, ProcessedEventItem } from "@/types/dashboard";

export function useRecentEvents() {
  return useQuery({
    queryKey: ["recent-events"],
    queryFn: async () =>
      (
        await api.get<PageResponse<ProcessedEventItem>>("/dashboard/events", {
          params: { page: 0, size: 20 },
        })
      ).data,
    refetchInterval: 3000,
  });
}
