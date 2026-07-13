package com.pulsequeue.notification;

import com.pulsequeue.config.PulseHubProperties;
import com.pulsequeue.entity.InstitutionMapping;
import com.pulsequeue.event.DomainEvent;
import com.pulsequeue.repository.InstitutionMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

/**
 * Bridges an event to a real PulseHub inbox: only acts when the payload
 * carries both {@code targetInstitutionId} and {@code message} — any other
 * event is a silent no-op, the same "notified regardless of type" shape as
 * this platform's Email/Push/WebSocket channels, just gated on payload shape
 * instead of always firing. Best-effort: a delivery failure is logged, never
 * rethrown, so an unreachable PulseHub instance can't turn an unrelated
 * event into a retry/DLQ entry.
 */
@Component
public class PulseHubNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(PulseHubNotificationChannel.class);

    private final InstitutionMappingRepository mappingRepository;
    private final RestClient restClient;
    private final String apiKey;

    public PulseHubNotificationChannel(InstitutionMappingRepository mappingRepository, PulseHubProperties properties,
                                        RestClient.Builder restClientBuilder) {
        this.mappingRepository = mappingRepository;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.apiKey = properties.apiKey();
    }

    @Override
    public String getChannelName() {
        return "PULSEHUB";
    }

    @Override
    public void send(DomainEvent event) {
        Object institutionIdRaw = event.payload().get("targetInstitutionId");
        Object messageRaw = event.payload().get("message");
        if (institutionIdRaw == null || messageRaw == null) {
            return;
        }

        Long institutionId = asLong(institutionIdRaw);
        if (institutionId == null) {
            log.warn("eventId={} has a non-numeric targetInstitutionId={}, skipping PulseHub delivery",
                    event.eventId(), institutionIdRaw);
            return;
        }

        Optional<InstitutionMapping> mapping = mappingRepository.findByInstitutionId(institutionId);
        if (mapping.isEmpty()) {
            log.info("No PulseHub mapping for institutionId={}, skipping delivery for eventId={}",
                    institutionId, event.eventId());
            return;
        }

        try {
            restClient.post()
                    .uri("/api/v1/system-messages")
                    .header("X-API-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "targetUserId", mapping.get().getPulsehubUserId(),
                            "content", messageRaw.toString()))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Delivered eventId={} to PulseHub userId={}", event.eventId(), mapping.get().getPulsehubUserId());
        } catch (RestClientException ex) {
            log.warn("Failed to deliver eventId={} to PulseHub (institutionId={}): {}",
                    event.eventId(), institutionId, ex.getMessage());
        }
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }
}
