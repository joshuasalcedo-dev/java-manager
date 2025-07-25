package io.joshuasalcedo.fx.domain.spec.maven;

import io.joshuasalcedo.fx.domain.model.project.Project;
import io.joshuasalcedo.fx.domain.spec.Specification;

/**
 * Specifications for Maven Project business rules.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class ProjectSpecifications {

    private ProjectSpecifications() {
        // Utility class
    }

    /**
     * A project must have coordinates (GAV)
     */
    public static Specification<Project> hasCoordinates() {
        return project -> project.getCoordinates() != null;
    }

    /**
     * A project must have a valid root path that exists
     */
    public static Specification<Project> hasValidRootPath() {
        return project -> {
            try {
                return project.getRoot() != null && project.getRoot().toFile().exists();
            } catch (Exception e) {
                return false;
            }
        };
    }

    /**
     * A project must have a pom.xml file
     */
    public static Specification<Project> hasPomFile() {
        return project -> {
            try {
                return project.getPomFile().exists();
            } catch (Exception e) {
                return false;
            }
        };
    }

    /**
     * A project without parent must have explicit groupId and version
     */
    public static Specification<Project> standaloneProjectHasCompleteCoordinates() {
        return project -> {
            if (project.getParent() != null) {
                return true; // Rule doesn't apply to child projects
            }
            
            var coords = project.getCoordinates();
            return coords != null && 
                   coords.getGroupId() != null && 
                   !coords.getGroupId().getValue().isEmpty() &&
                   coords.getVersion() != null && 
                   !coords.getVersion().getValue().isEmpty();
        };
    }

    /**
     * A child project can inherit groupId and version from parent
     */
    public static Specification<Project> childProjectCanInheritFromParent() {
        return project -> {
            if (project.getParent() == null) {
                return true; // Rule doesn't apply to standalone projects
            }
            
            // Child project is valid if it has at least artifactId
            var coords = project.getCoordinates();
            return coords != null && 
                   coords.getArtifactId() != null && 
                   !coords.getArtifactId().getValue().isEmpty();
        };
    }

    /**
     * A project is a valid Maven project
     */
    public static Specification<Project> isValidMavenProject() {
        return hasCoordinates()
                .and(hasValidRootPath())
                .and(hasPomFile())
                .and(standaloneProjectHasCompleteCoordinates().or(childProjectCanInheritFromParent()));
    }

    /**
     * A project is a multi-module project if it has modules
     */
    public static Specification<Project> isMultiModuleProject() {
        return project -> project.getModules() != null && !project.getModules().isEmpty();
    }

    /**
     * A project is a leaf project (has no modules)
     */
    public static Specification<Project> isLeafProject() {
        return project -> project.getModules() == null || project.getModules().isEmpty();
    }


    /**
     * A project has dependencies
     */
    public static Specification<Project> hasDependencies() {
        return project -> project.getDependencies() != null && !project.getDependencies().isEmpty();
    }

    /**
     * A project is a snapshot project
     */
    public static Specification<Project> isSnapshotProject() {
        return project -> {
            var version = project.getCoordinates().getVersion();
            return version != null && version.isSnapshot();
        };
    }

    /**
     * A project is a release project
     */
    public static Specification<Project> isReleaseProject() {
        return project -> {
            var version = project.getCoordinates().getVersion();
            return version != null && version.isRelease();
        };
    }
}