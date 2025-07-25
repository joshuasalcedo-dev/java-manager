package io.joshuasalcedo.fx.domain.spec.maven;

import io.joshuasalcedo.fx.domain.model.project.Coordinates;
import io.joshuasalcedo.fx.domain.spec.Specification;

/**
 * Specifications for Maven Coordinates business rules.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class CoordinatesSpecifications {

    private CoordinatesSpecifications() {
        // Utility class
    }

    /**
     * Coordinates must have a valid artifactId
     */
    public static Specification<Coordinates> hasValidArtifactId() {
        return coordinates -> 
            coordinates.getArtifactId() != null && 
            !coordinates.getArtifactId().getValue().isEmpty();
    }

    /**
     * Coordinates must have a valid groupId
     */
    public static Specification<Coordinates> hasValidGroupId() {
        return coordinates -> 
            coordinates.getGroupId() != null && 
            !coordinates.getGroupId().getValue().isEmpty();
    }

    /**
     * Coordinates must have a valid version
     */
    public static Specification<Coordinates> hasValidVersion() {
        return coordinates -> 
            coordinates.getVersion() != null && 
            !coordinates.getVersion().getValue().isEmpty();
    }

    /**
     * Coordinates are complete (GAV)
     */
    public static Specification<Coordinates> isComplete() {
        return hasValidGroupId()
                .and(hasValidArtifactId())
                .and(hasValidVersion());
    }

    /**
     * Coordinates are partial (GA only)
     */
    public static Specification<Coordinates> isPartial() {
        return hasValidGroupId()
                .and(hasValidArtifactId())
                .and(hasValidVersion().not());
    }

    /**
     * Coordinates use a snapshot version
     */
    public static Specification<Coordinates> isSnapshot() {
        return coordinates -> 
            coordinates.getVersion() != null && 
            coordinates.getVersion().isSnapshot();
    }

    /**
     * Coordinates use a release version
     */
    public static Specification<Coordinates> isRelease() {
        return coordinates -> 
            coordinates.getVersion() != null && 
            coordinates.getVersion().isRelease();
    }

    /**
     * Coordinates use a property version
     */
    public static Specification<Coordinates> usesPropertyVersion() {
        return coordinates -> 
            coordinates.getVersion() != null && 
            coordinates.getVersion().isProperty();
    }

    /**
     * Coordinates can be converted to GAV string
     */
    public static Specification<Coordinates> canConvertToGAV() {
        return isComplete();
    }

    /**
     * Coordinates can be converted to GA string
     */
    public static Specification<Coordinates> canConvertToGA() {
        return hasValidGroupId().and(hasValidArtifactId());
    }
}