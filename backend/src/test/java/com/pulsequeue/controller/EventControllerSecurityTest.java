package com.pulsequeue.controller;

import com.pulsequeue.producer.EventPublisher;
import com.pulsequeue.security.ApiKeyAuthFilter;
import com.pulsequeue.security.ApiKeyProperties;
import com.pulsequeue.security.SecurityConfig;
import com.pulsequeue.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the shared-secret guard on the publish endpoints. The rest of
 * EventController's behavior (validation, rate limiting, event shape) isn't
 * re-tested here — this class exists solely to prove the security layer
 * itself works, at the boundary where it actually matters (a real MockMvc
 * request going through the real filter chain, not a mocked-away concern).
 */
@WebMvcTest(EventController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties(ApiKeyProperties.class)
@TestPropertySource(properties = "pulsequeue.security.api-key=test-secret-key")
class EventControllerSecurityTest {

    private static final String VALID_PAYLOAD = """
            {"eventType":"expense.created","sourceService":"cashpilot","payload":{"amount":10}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventPublisher publisher;
    @MockBean
    private RateLimitService rateLimitService;

    @Test
    void rejectsPublishRequestWithoutApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsPublishRequestWithWrongApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsPublishRequestWithCorrectApiKey() throws Exception {
        when(rateLimitService.tryAcquire(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/events")
                        .header(ApiKeyAuthFilter.API_KEY_HEADER, "test-secret-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isAccepted());
    }

    @Test
    void simulateEndpointAlsoRequiresApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/events/simulate"))
                .andExpect(status().isUnauthorized());
    }
}
