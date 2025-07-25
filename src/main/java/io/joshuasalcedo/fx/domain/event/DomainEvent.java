package io.joshuasalcedo.fx.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in the Maven management domain.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public interface DomainEvent {
    
    /**
     * Unique identifier for this event
     */
    UUID getEventId();
    
    /**
     * When this event occurred
     */
    Instant getOccurredOn();
    
    /**
     * Version of the event schema (for evolution)
     */
    default int getEventVersion() {
        return 1;
    }
    
    /**
     * Type identifier for this event
     */
    String getEventType();
}