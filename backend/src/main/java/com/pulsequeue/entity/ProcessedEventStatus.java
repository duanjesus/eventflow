package com.pulsequeue.entity;

public enum ProcessedEventStatus {
    RECEIVED,
    PROCESSED,
    FAILED,
    DEAD_LETTERED,
    DUPLICATE
}
