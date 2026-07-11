import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { DashboardStats } from "@/types/dashboard";

export function useDashboardStats() {
  return useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: async () => (await api.get<DashboardStats>("/dashboard/stats")).data,
    refetchInterval: 3000,
  });
}
