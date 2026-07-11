package com.eventflow;

import com.eventflow.entity.ProcessedEvent;
import com.eventflow.entity.ProcessedEventStatus;
import com.eventflow.event.DomainEvent;
import com.eventflow.producer.EventPublisher;
import com.eventflow.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end proof that the pipeline actually works against real
 * Postgres/RabbitMQ/Redis, not just mocked collaborators: publish -> queue
 * -> consumer -> dedup/notify -> recorded outcome, including the retry+DLQ
 * path and the duplicate-delivery path.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EventFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("eventflow_db")
            .withUsername("eventflow_user")
            .withPassword("eventflow_pass");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management-alpine"));

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    private EventPublisher publisher;
    @Autowired
    private ProcessedEventRepository repository;

    @Test
    void publishedEventIsConsumedAndMarkedProcessed() {
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("amount", 42));

        publisher.publish(event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<ProcessedEvent> record = repository.findByEventId(event.eventId());
            assertTrue(record.isPresent(), "event should have been recorded by now");
            assertEquals(ProcessedEventStatus.PROCESSED, record.get().getStatus());
        });
    }

    @Test
    void eventThatAlwaysFailsExhaustsRetriesAndLandsOnTheDeadLetterQueue() {
        DomainEvent event = DomainEvent.of("expense.created", "cashpilot", Map.of("simulateFailure", true));

        publisher.publish(event);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            Optional<ProcessedEvent> record = repository.findByEventId(event.eventId());
            assertTrue(record.isPresent(), "event should have been recorded by now");
            assertEquals(ProcessedEventStatus.DEAD_LETTERED, record.get().getStatus());
            assertTrue(record.get().getRetryCount() >= 1);
        });
    }

    @Test
    void duplicateEventIdIsSkippedOnSecondDelivery() {
        String eventId = UUID.randomUUID().toString();
        DomainEvent event = new DomainEvent(eventId, "expense.created", "cashpilot", Map.of("amount", 1), Instant.now());

        publisher.publish(event);
        await().atMost(Duration.ofSeconds(10)).until(() ->
                repository.findByEventId(eventId).map(r -> r.getStatus() == ProcessedEventStatus.PROCESSED).orElse(false));

        publisher.publish(event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Optional<ProcessedEvent> record = repository.findByEventId(eventId);
            assertTrue(record.isPresent(), "duplicate delivery should still have a record");
            assertEquals(ProcessedEventStatus.DUPLICATE, record.get().getStatus());
        });
    }
}
