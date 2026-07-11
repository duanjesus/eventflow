package com.eventflow.repository;

import com.eventflow.entity.ProcessedEvent;
import com.eventflow.entity.ProcessedEventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    Optional<ProcessedEvent> findByEventId(String eventId);

    long countByStatus(ProcessedEventStatus status);

    Page<ProcessedEvent> findAllByOrderByReceivedAtDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.retryCount), 0) FROM ProcessedEvent p")
    long sumRetryCount();
}
