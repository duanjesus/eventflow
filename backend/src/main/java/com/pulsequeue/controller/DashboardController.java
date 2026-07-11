package com.pulsequeue.controller;

import com.pulsequeue.dto.response.DashboardStatsResponse;
import com.pulsequeue.dto.response.ProcessedEventResponse;
import com.pulsequeue.repository.ProcessedEventRepository;
import com.pulsequeue.service.DashboardStatsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardStatsService statsService;
    private final ProcessedEventRepository repository;

    public DashboardController(DashboardStatsService statsService, ProcessedEventRepository repository) {
        this.statsService = statsService;
        this.repository = repository;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        return statsService.getStats();
    }

    @GetMapping("/events")
    public Page<ProcessedEventResponse> recentEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return repository.findAllByOrderByReceivedAtDesc(PageRequest.of(page, size))
                .map(ProcessedEventResponse::from);
    }
}
