package io.joshuasalcedo.fx.domain.spec.maven;

import io.joshuasalcedo.fx.domain.model.project.Dependency;
import io.joshuasalcedo.fx.domain.spec.Specification;

/**
 * Specifications for Maven Dependency business rules.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class DependencySpecifications {

    private DependencySpecifications() {
        // Utility class
    }

    /**
     * A dependency must have groupId and artifactId
     */
    public static Specification<Dependency> hasRequiredCoordinates() {
        return dependency -> 
            dependency.getGroupId() != null && 
            !dependency.getGroupId().getValue().isEmpty() &&
            dependency.getArtifactId() != null && 
            !dependency.getArtifactId().getValue().isEmpty();
    }

    /**
     * A dependency has an explicit version
     */
    public static Specification<Dependency> hasExplicitVersion() {
        return dependency -> 
            dependency.getVersion().isPresent() && 
            !dependency.getVersion().get().getValue().isEmpty();
    }

    /**
     * A dependency uses a property version
     */
    public static Specification<Dependency> usesPropertyVersion() {
        return dependency -> 
            dependency.getVersion().isPresent() && 
            dependency.getVersion().get().isProperty();
    }

    /**
     * A dependency is a snapshot dependency
     */
    public static Specification<Dependency> isSnapshotDependency() {
        return dependency -> 
            dependency.getVersion().isPresent() && 
            dependency.getVersion().get().isSnapshot();
    }

    /**
     * A dependency is a release dependency
     */
    public static Specification<Dependency> isReleaseDependency() {
        return dependency -> 
            dependency.getVersion().isPresent() && 
            dependency.getVersion().get().isRelease();
    }

    /**
     * A dependency has a scope
     */
    public static Specification<Dependency> hasScope() {
        return dependency -> dependency.getScope().isPresent();
    }

    /**
     * A dependency is a test dependency
     */
    public static Specification<Dependency> isTestDependency() {
        return dependency -> 
            dependency.getScope().isPresent() && 
            "test".equalsIgnoreCase(dependency.getScope().get().getValue());
    }

    /**
     * A dependency is a compile dependency (default scope)
     */
    public static Specification<Dependency> isCompileDependency() {
        return dependency -> 
            dependency.getScope().isEmpty() || 
            "compile".equalsIgnoreCase(dependency.getScope().get().getValue());
    }

    /**
     * A dependency is a valid Maven dependency
     */
    public static Specification<Dependency> isValidMavenDependency() {
        return hasRequiredCoordinates();
    }

    /**
     * A dependency can be converted to version property
     */
    public static Specification<Dependency> canBeConvertedToVersionProperty() {
        return hasExplicitVersion().and(usesPropertyVersion().not());
    }

    /**
     * A dependency needs version resolution (missing version or property version)
     */
    public static Specification<Dependency> needsVersionResolution() {
        return dependency -> 
            dependency.getVersion().isEmpty() || 
            dependency.getVersion().get().isProperty();
    }
}