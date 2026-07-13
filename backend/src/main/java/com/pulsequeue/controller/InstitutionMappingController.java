package com.pulsequeue.controller;

import com.pulsequeue.dto.request.InstitutionMappingRequest;
import com.pulsequeue.dto.response.InstitutionMappingResponse;
import com.pulsequeue.entity.InstitutionMapping;
import com.pulsequeue.repository.InstitutionMappingRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-style CRUD for the institutionId -> pulsehubUserId join that
 * PulseHubNotificationChannel reads when routing an event to a real PulseHub
 * inbox. Not producer-only (unlike /api/v1/events) — same permitAll() as the
 * rest of the dashboard-facing API, no write-auth stakes here yet.
 */
@RestController
@RequestMapping("/api/v1/institution-mappings")
public class InstitutionMappingController {

    private final InstitutionMappingRepository repository;

    public InstitutionMappingController(InstitutionMappingRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<InstitutionMappingResponse> upsert(@Valid @RequestBody InstitutionMappingRequest request) {
        InstitutionMapping mapping = repository.findByInstitutionId(request.institutionId())
                .map(existing -> {
                    existing.remap(request.pulsehubUserId());
                    return existing;
                })
                .orElseGet(() -> InstitutionMapping.of(request.institutionId(), request.pulsehubUserId()));

        InstitutionMapping saved = repository.save(mapping);
        return ResponseEntity.status(HttpStatus.OK).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<InstitutionMappingResponse>> list() {
        List<InstitutionMappingResponse> mappings = repository.findAllByOrderByInstitutionIdAsc().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(mappings);
    }

    private InstitutionMappingResponse toResponse(InstitutionMapping mapping) {
        return new InstitutionMappingResponse(mapping.getInstitutionId(), mapping.getPulsehubUserId(), mapping.getCreatedAt());
    }
}
