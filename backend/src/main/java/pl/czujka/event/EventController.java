package pl.czujka.event;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
class EventController {

    private final EventRepository repository;

    EventController(EventRepository repository) {
        this.repository = repository;
    }

    /**
     * Active events in the given area, e.g. {@code GET /api/events?bbox=14.07,49.0,24.15,54.84}.
     * Without {@code bbox} the whole of Poland is used.
     */
    @GetMapping
    List<ThreatEvent> activeEvents(@RequestParam(required = false) String bbox) {
        BoundingBox box = bbox == null ? BoundingBox.POLAND : BoundingBox.parse(bbox);
        return repository.findActiveIn(box);
    }
}
