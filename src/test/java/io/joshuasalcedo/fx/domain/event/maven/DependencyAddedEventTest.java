package io.joshuasalcedo.fx.domain.event.maven;

import io.joshuasalcedo.fx.domain.model.project.Coordinates;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DependencyAddedEventTest {

    @Test
    void testConstructorAndGetters() {
        UUID eventId = UUID.randomUUID();
        Instant occurredOn = Instant.now();
        Coordinates projectCoordinates = new Coordinates("io.joshuasalcedo", "test-project", "1.0.0");
        String dependencyGA = "org.springframework:spring-core";
        String dependencyVersion = "5.3.9";
        String scope = "compile";
        
        DependencyAddedEvent event = new DependencyAddedEvent(eventId, occurredOn, projectCoordinates, 
                                                            dependencyGA, dependencyVersion, scope);
        
        assertEquals(eventId, event.getEventId());
        assertEquals(occurredOn, event.getOccurredOn());
        assertEquals(projectCoordinates, event.getProjectCoordinates());
        assertEquals(dependencyGA, event.getDependencyGA());
        assertEquals(dependencyVersion, event.getDependencyVersion());
        assertEquals(scope, event.getScope());
        assertEquals("DependencyAddedEvent", event.getEventType());
        assertEquals(1, event.getEventVersion());
    }
    
    @Test
    void testFactoryMethod() {
        Coordinates projectCoordinates = new Coordinates("io.joshuasalcedo", "test-project", "1.0.0");
        String dependencyGA = "org.springframework:spring-core";
        String dependencyVersion = "5.3.9";
        String scope = "test";
        
        DependencyAddedEvent event = DependencyAddedEvent.create(projectCoordinates, dependencyGA, 
                                                               dependencyVersion, scope);
        
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
        assertEquals(projectCoordinates, event.getProjectCoordinates());
        assertEquals(dependencyGA, event.getDependencyGA());
        assertEquals(dependencyVersion, event.getDependencyVersion());
        assertEquals(scope, event.getScope());
    }
    
    @Test
    void testNullHandling() {
        Coordinates projectCoordinates = new Coordinates("io.joshuasalcedo", "test-project", "1.0.0");
        String dependencyGA = "org.springframework:spring-core";
        String dependencyVersion = "5.3.9";
        String scope = "provided";
        
        DependencyAddedEvent event = new DependencyAddedEvent(null, null, projectCoordinates, 
                                                            dependencyGA, dependencyVersion, scope);
        
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredOn());
        assertEquals(projectCoordinates, event.getProjectCoordinates());
        assertEquals(dependencyGA, event.getDependencyGA());
        assertEquals(dependencyVersion, event.getDependencyVersion());
        assertEquals(scope, event.getScope());
    }
}