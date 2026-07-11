package com.eventflow.entity;

public enum ProcessedEventStatus {
    RECEIVED,
    PROCESSED,
    FAILED,
    DEAD_LETTERED,
    DUPLICATE
}
