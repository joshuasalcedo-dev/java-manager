package io.joshuasalcedo.fx.domain.event.maven;

import io.joshuasalcedo.fx.domain.model.project.Coordinates;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectCreatedEventTest {

    @Test
    void testConstructorAndGetters() {
        UUID eventId = UUID.randomUUID();
        Instant occurredOn = Instant.now();
        Coordinates coordinates = new Coordinates("io.joshuasalcedo", "test-project", "1.0.0");
        String rootPath = "/path/to/project";
        boolean hasParent = false;

        ProjectCreatedEvent event = new ProjectCreatedEvent(eventId, occurredOn, coordinates, rootPath, hasParent);

        assertEquals(eventId, event.getEventId());
        assertEquals(occurredOn, event.getOccurredOn());
        assertEquals(coordinates, event.getProjectCoordinates());
        assertEquals(rootPath, event.getRootPath());
        assertEquals(hasParent, event.isHasParent());
        assertEquals("ProjectCreatedEvent", event.getEventType());
        assertEquals(1, event.getEventVersion());
    }

    @Test
    void testFactoryMethod() {
        Coordinates coordinates = new Coordinates("io.joshuasalcedo", "test-project", "1.0.0");
        String rootPath = "/path/to/project";
        boolean hasParent = true;

        ProjectCreatedEvent event = ProjectCreatedEvent.create(coordinates, rootPath, hasParent);

        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
        assertEquals(coordinates, event.getProjectCoordinates());
        assertEquals(rootPath, event.getRootPath());
        assertEquals(hasParent, event.isHasParent());
    }

    @Test
    void testNullHandling() {
        Coordinates coordinates = new Coordinates("io.joshuasalcedo", "test-project", "1.0.0");
        String rootPath = "/path/to/project";
        boolean hasParent = false;

        ProjectCreatedEvent event = new ProjectCreatedEvent(null, null, coordinates, rootPath, hasParent);

        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
        assertEquals(coordinates, event.getProjectCoordinates());
        assertEquals(rootPath, event.getRootPath());
        assertEquals(hasParent, event.isHasParent());
    }
}
