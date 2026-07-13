package com.pulsequeue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Links a Social Supply institution to the PulseHub user id that should
 * receive system messages on its behalf — the join PulseHubNotificationChannel
 * uses to route a DomainEvent's targetInstitutionId to a real PulseHub inbox.
 */
@Entity
@Table(name = "institution_mapping")
public class InstitutionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false, unique = true)
    private Long institutionId;

    @Column(name = "pulsehub_user_id", nullable = false)
    private Long pulsehubUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected InstitutionMapping() {
        // JPA
    }

    public static InstitutionMapping of(Long institutionId, Long pulsehubUserId) {
        InstitutionMapping mapping = new InstitutionMapping();
        mapping.institutionId = institutionId;
        mapping.pulsehubUserId = pulsehubUserId;
        mapping.createdAt = Instant.now();
        return mapping;
    }

    public void remap(Long pulsehubUserId) {
        this.pulsehubUserId = pulsehubUserId;
    }

    public Long getId() {
        return id;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public Long getPulsehubUserId() {
        return pulsehubUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
