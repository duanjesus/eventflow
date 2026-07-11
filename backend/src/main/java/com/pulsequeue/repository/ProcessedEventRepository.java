package com.pulsequeue.repository;

import com.pulsequeue.entity.ProcessedEvent;
import com.pulsequeue.entity.ProcessedEventStatus;
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
