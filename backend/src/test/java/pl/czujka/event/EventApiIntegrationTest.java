package pl.czujka.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;

import pl.czujka.TestcontainersConfiguration;

@SpringBootTest(properties = "czujka.ingestion.enabled=false")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EventApiIntegrationTest {

    private static final Instant NOW = Instant.now();

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EventRepository repository;

    @Autowired
    JdbcClient jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.sql("DELETE FROM threat_event").update();
    }

    @Test
    void returnsActiveEventsInsideBoundingBox() throws Exception {
        repository.upsert(event("lublin", 51.25, 22.57, NOW.plus(Duration.ofHours(1))));
        repository.upsert(event("berlin", 52.52, 13.40, NOW.plus(Duration.ofHours(1))));
        repository.upsert(event("expired", 52.23, 21.01, NOW.minus(Duration.ofHours(1))));

        mockMvc.perform(get("/api/events").param("bbox", "14.07,49.0,24.15,54.84"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].externalId").value("lublin"))
                .andExpect(jsonPath("$[0].latitude").value(51.25))
                .andExpect(jsonPath("$[0].longitude").value(22.57));
    }

    @Test
    void upsertUpdatesExistingEventInsteadOfDuplicatingIt() {
        repository.upsert(event("lublin", 51.25, 22.57, null));
        repository.upsert(new ThreatEvent("test", "lublin", EventCategory.AIR_THREAT, Severity.CRITICAL,
                "Updated title", null, 51.25, 22.57, NOW, null, null));

        var events = repository.findActiveIn(BoundingBox.POLAND);

        assertThat(events).singleElement().satisfies(e -> {
            assertThat(e.title()).isEqualTo("Updated title");
            assertThat(e.severity()).isEqualTo(Severity.CRITICAL);
        });
    }

    @Test
    void rejectsInvalidBoundingBox() throws Exception {
        mockMvc.perform(get("/api/events").param("bbox", "not,a,valid,box"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("bbox must contain four numbers"));
    }

    private static ThreatEvent event(String id, double latitude, double longitude, Instant endsAt) {
        return new ThreatEvent("test", id, EventCategory.AIR_THREAT, Severity.HIGH, "Event " + id, null,
                latitude, longitude, NOW, endsAt, null);
    }
}
