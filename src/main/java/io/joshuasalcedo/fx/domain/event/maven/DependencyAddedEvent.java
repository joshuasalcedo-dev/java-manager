package io.joshuasalcedo.fx.domain.event.maven;

import io.joshuasalcedo.fx.domain.event.DomainEvent;
import io.joshuasalcedo.fx.domain.model.project.Coordinates;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event fired when a dependency is added to a Maven project.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public class DependencyAddedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final Coordinates projectCoordinates;
    private final String dependencyGA;
    private final String dependencyVersion;
    private final String scope;
    
    public DependencyAddedEvent(UUID eventId, Instant occurredOn, Coordinates projectCoordinates,
                              String dependencyGA, String dependencyVersion, String scope) {
        this.eventId = (eventId == null) ? UUID.randomUUID() : eventId;
        this.occurredOn = (occurredOn == null) ? Instant.now() : occurredOn;
        this.projectCoordinates = projectCoordinates;
        this.dependencyGA = dependencyGA;
        this.dependencyVersion = dependencyVersion;
        this.scope = scope;
    }
    
    public static DependencyAddedEvent create(Coordinates projectCoordinates, String dependencyGA, 
                                            String dependencyVersion, String scope) {
        return new DependencyAddedEvent(null, null, projectCoordinates, dependencyGA, dependencyVersion, scope);
    }
    
    @Override
    public UUID getEventId() {
        return eventId;
    }
    
    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
    
    @Override
    public String getEventType() {
        return "DependencyAddedEvent";
    }
    
    public Coordinates getProjectCoordinates() {
        return projectCoordinates;
    }
    
    public String getDependencyGA() {
        return dependencyGA;
    }
    
    public String getDependencyVersion() {
        return dependencyVersion;
    }
    
    public String getScope() {
        return scope;
    }
}