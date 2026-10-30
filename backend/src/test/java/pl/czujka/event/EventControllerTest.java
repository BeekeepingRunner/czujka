package pl.czujka.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web layer only: request parsing, the JSON contract the frontend relies on, and error responses.
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EventRepository repository;

    @Test
    void usesWholePolandWhenBboxIsMissing() throws Exception {
        mockMvc.perform(get("/api/events")).andExpect(status().isOk());

        verify(repository).findActiveIn(BoundingBox.POLAND);
    }

    @Test
    void passesParsedBboxToRepository() throws Exception {
        mockMvc.perform(get("/api/events").param("bbox", "20.5,50,21.5,51")).andExpect(status().isOk());

        verify(repository).findActiveIn(new BoundingBox(20.5, 50, 21.5, 51));
    }

    @Test
    void returnsEmptyArrayWhenThereAreNoEvents() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", JsonCompareMode.STRICT));
    }

    @Test
    void serialisesEventsInTheFormatTheFrontendExpects() throws Exception {
        given(repository.findActiveIn(any())).willReturn(List.of(
                new ThreatEvent("rcb", "alert-1", EventCategory.CIVIL_ALERT, Severity.HIGH, "Alert RCB",
                        "Stay at home", 51.25, 22.57, Instant.parse("2026-10-16T10:00:00Z"),
                        Instant.parse("2026-10-16T12:30:00Z"), "https://example.org/alert/1"),
                new ThreatEvent("mock", "minimal", EventCategory.OTHER, Severity.LOW, "Minimal", null,
                        52.0, 19.0, Instant.parse("2026-10-16T10:00:00Z"), null, null)));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                          {
                            "source": "rcb", "externalId": "alert-1", "category": "CIVIL_ALERT", "severity": "HIGH",
                            "title": "Alert RCB", "description": "Stay at home", "latitude": 51.25, "longitude": 22.57,
                            "startsAt": "2026-10-16T10:00:00Z", "endsAt": "2026-10-16T12:30:00Z",
                            "url": "https://example.org/alert/1"
                          },
                          {
                            "source": "mock", "externalId": "minimal", "category": "OTHER", "severity": "LOW",
                            "title": "Minimal", "description": null, "latitude": 52.0, "longitude": 19.0,
                            "startsAt": "2026-10-16T10:00:00Z", "endsAt": null, "url": null
                          }
                        ]
                        """, JsonCompareMode.STRICT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not,a,valid,box", "1,2,3", "NaN,49,24,54", "24,49,14,54", "14,49,200,54"})
    void rejectsInvalidBboxWithProblemDetail(String bbox) throws Exception {
        mockMvc.perform(get("/api/events").param("bbox", bbox))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/api/events"));

        verify(repository, never()).findActiveIn(any());
    }
}
