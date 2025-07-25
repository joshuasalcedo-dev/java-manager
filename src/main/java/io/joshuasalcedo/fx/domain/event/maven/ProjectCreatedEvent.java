package io.joshuasalcedo.fx.domain.event.maven;

import io.joshuasalcedo.fx.domain.event.DomainEvent;
import io.joshuasalcedo.fx.domain.model.project.Coordinates;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event fired when a Maven project is created.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public class ProjectCreatedEvent implements DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final Coordinates projectCoordinates;
    private final String rootPath;
    private final boolean hasParent;
    
    public ProjectCreatedEvent(UUID eventId, Instant occurredOn, Coordinates projectCoordinates, 
                             String rootPath, boolean hasParent) {
        this.eventId = (eventId == null) ? UUID.randomUUID() : eventId;
        this.occurredOn = (occurredOn == null) ? Instant.now() : occurredOn;
        this.projectCoordinates = projectCoordinates;
        this.rootPath = rootPath;
        this.hasParent = hasParent;
    }
    
    public static ProjectCreatedEvent create(Coordinates coordinates, String rootPath, boolean hasParent) {
        return new ProjectCreatedEvent(null, null, coordinates, rootPath, hasParent);
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
        return "ProjectCreatedEvent";
    }
    
    public Coordinates getProjectCoordinates() {
        return projectCoordinates;
    }
    
    public String getRootPath() {
        return rootPath;
    }
    
    public boolean isHasParent() {
        return hasParent;
    }
}