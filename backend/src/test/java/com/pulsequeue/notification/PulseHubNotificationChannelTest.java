package com.pulsequeue.notification;

import com.pulsequeue.config.PulseHubProperties;
import com.pulsequeue.entity.InstitutionMapping;
import com.pulsequeue.event.DomainEvent;
import com.pulsequeue.repository.InstitutionMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PulseHubNotificationChannelTest {

    private InstitutionMappingRepository mappingRepository;
    private MockRestServiceServer server;
    private PulseHubNotificationChannel channel;

    @BeforeEach
    void setUp() {
        mappingRepository = mock(InstitutionMappingRepository.class);
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        PulseHubProperties properties = new PulseHubProperties("http://pulsehub", "test-key");
        channel = new PulseHubNotificationChannel(mappingRepository, properties, builder);
    }

    @Test
    void skipsSilentlyWhenPayloadHasNoTargetInstitutionOrMessage() {
        DomainEvent event = DomainEvent.of("donation.created", "social-supply", Map.of("donationId", 1));

        channel.send(event);

        verifyNoInteractions(mappingRepository);
        server.verify();
    }

    @Test
    void skipsSilentlyWhenInstitutionHasNoPulseHubMapping() {
        when(mappingRepository.findByInstitutionId(7L)).thenReturn(Optional.empty());
        DomainEvent event = DomainEvent.of("supply.surplus_alert", "social-supply",
                Map.of("targetInstitutionId", 7, "message", "Sobra de arroz"));

        channel.send(event);

        verify(mappingRepository).findByInstitutionId(7L);
        server.verify();
    }

    @Test
    void deliversToPulseHubWhenMappingExists() {
        when(mappingRepository.findByInstitutionId(7L)).thenReturn(Optional.of(InstitutionMapping.of(7L, 42L)));
        DomainEvent event = DomainEvent.of("supply.surplus_alert", "social-supply",
                Map.of("targetInstitutionId", 7, "message", "Sobra de arroz"));

        server.expect(requestTo("http://pulsehub/api/v1/system-messages"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("X-API-Key", "test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"targetUserId\":42,\"content\":\"Sobra de arroz\"}"))
                .andRespond(withSuccess());

        channel.send(event);

        server.verify();
    }

    @Test
    void swallowsDeliveryFailuresInsteadOfThrowing() {
        when(mappingRepository.findByInstitutionId(7L)).thenReturn(Optional.of(InstitutionMapping.of(7L, 42L)));
        DomainEvent event = DomainEvent.of("supply.surplus_alert", "social-supply",
                Map.of("targetInstitutionId", 7, "message", "Sobra de arroz"));

        server.expect(requestTo("http://pulsehub/api/v1/system-messages"))
                .andRespond(withServerError());

        channel.send(event);

        server.verify();
    }
}
