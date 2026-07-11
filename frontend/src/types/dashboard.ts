export type ProcessedEventStatus =
  | "RECEIVED"
  | "PROCESSED"
  | "FAILED"
  | "DEAD_LETTERED"
  | "DUPLICATE";

export interface DashboardStats {
  queueDepth: number;
  totalReceived: number;
  processed: number;
  failed: number;
  deadLettered: number;
  duplicate: number;
  totalRetries: number;
}

export interface ProcessedEventItem {
  id: number;
  eventId: string;
  eventType: string;
  sourceService: string;
  status: ProcessedEventStatus;
  retryCount: number;
  errorMessage: string | null;
  receivedAt: string;
  processedAt: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface DomainEventMessage {
  eventId: string;
  eventType: string;
  sourceService: string;
  payload: Record<string, unknown>;
  occurredAt: string;
}
