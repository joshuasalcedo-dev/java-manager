package io.joshuasalcedo.fx.domain.service;

import io.joshuasalcedo.fx.domain.model.project.Coordinates;
import io.joshuasalcedo.fx.domain.model.project.Dependency;
import io.joshuasalcedo.fx.domain.model.project.Project;
import io.joshuasalcedo.fx.domain.spec.maven.ProjectSpecifications;
import io.joshuasalcedo.fx.domain.value.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Domain service for complex Maven Project business logic.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public class ProjectDomainService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectDomainService.class);

    /**
     * Validates if a project follows Maven conventions and business rules
     */
    public ProjectValidationResult validateProject(Project project) {
        logger.debug("Validating project: {}", project.getCoordinates().toGAV());
        
        var validationResult = new ProjectValidationResult();
        
        // Check if project is a valid Maven project
        if (!ProjectSpecifications.isValidMavenProject().isSatisfiedBy(project)) {
            validationResult.addError("Project does not satisfy Maven project requirements");
        }
        
        // Check coordinates
        if (!ProjectSpecifications.hasCoordinates().isSatisfiedBy(project)) {
            validationResult.addError("Project must have coordinates");
        }
        
        // Check root path
        if (!ProjectSpecifications.hasValidRootPath().isSatisfiedBy(project)) {
            validationResult.addError("Project must have a valid root path");
        }
        
        // Check pom.xml
        if (!ProjectSpecifications.hasPomFile().isSatisfiedBy(project)) {
            validationResult.addError("Project must have a pom.xml file");
        }
        
        // Check parent/child relationship rules
        if (project.getParent() == null && 
            !ProjectSpecifications.standaloneProjectHasCompleteCoordinates().isSatisfiedBy(project)) {
            validationResult.addError("Standalone project must have complete coordinates (groupId, artifactId, version)");
        }
        
        if (project.getParent() != null && 
            !ProjectSpecifications.childProjectCanInheritFromParent().isSatisfiedBy(project)) {
            validationResult.addError("Child project must have at least artifactId defined");
        }
        
        logger.debug("Project validation completed with {} errors", validationResult.getErrors().size());
        return validationResult;
    }

    /**
     * Resolves dependency versions using project properties and parent inheritance
     */
    public List<Dependency> resolveDependencyVersions(Project project, List<Dependency> dependencies) {
        logger.debug("Resolving dependency versions for project: {}", project.getCoordinates().toGAV());
        
        Map<String, String> properties = project.getProperties();
        
        return dependencies.stream()
                .map(dependency -> resolveDependencyVersion(dependency, properties, project))
                .toList();
    }

    private Dependency resolveDependencyVersion(Dependency dependency, Map<String, String> properties, Project project) {
        // If dependency already has a concrete version, return as-is
        if (dependency.getVersion().isPresent() && !dependency.getVersion().get().isProperty()) {
            return dependency;
        }
        
        // Try to resolve property version
        if (dependency.getVersion().isPresent() && dependency.getVersion().get().isProperty()) {
            String propertyName = dependency.getVersion().get().getPropertyName();
            if (properties.containsKey(propertyName)) {
                String resolvedVersion = properties.get(propertyName);
                logger.debug("Resolved property {} to version {} for dependency {}", 
                        propertyName, resolvedVersion, dependency.toGAV());
                return new Dependency(dependency.getGroupId(), dependency.getArtifactId(), 
                        new Version(resolvedVersion));
            }
        }
        
        // Try to inherit version from parent
        if (dependency.getVersion().isEmpty() && project.getParent() != null) {
            String parentVersion = project.getParent().coordinates().getVersion().getValue();
            logger.debug("Inherited version {} from parent for dependency {}", 
                    parentVersion, dependency.toGA());
            return new Dependency(dependency.getGroupId(), dependency.getArtifactId(), 
                    new Version(parentVersion));
        }
        
        logger.warn("Could not resolve version for dependency: {}", dependency.toGA());
        return dependency;
    }

    /**
     * Converts dependencies to use version properties
     */
    public ConversionResult convertDependenciesToVersionProperties(Project project, List<Dependency> dependencies) {
        logger.debug("Converting dependencies to version properties for project: {}", 
                project.getCoordinates().toGAV());
        
        var result = new ConversionResult();
        Map<String, String> versionProperties = result.getVersionProperties();
        
        for (Dependency dependency : dependencies) {
            if (canConvertToVersionProperty(dependency)) {
                Dependency converted = dependency.toVersionPropertyDependency(versionProperties);
                result.addConvertedDependency(converted);
                logger.debug("Converted dependency {} to use version property", dependency.toGAV());
            } else {
                result.addUnchangedDependency(dependency);
            }
        }
        
        logger.debug("Conversion completed: {} converted, {} unchanged", 
                result.getConvertedDependencies().size(), 
                result.getUnchangedDependencies().size());
        
        return result;
    }

    private boolean canConvertToVersionProperty(Dependency dependency) {
        return dependency.getVersion().isPresent() && 
               !dependency.getVersion().get().isProperty() &&
               !dependency.getVersion().get().getValue().isEmpty();
    }

    /**
     * Determines the effective coordinates for a project (considering parent inheritance)
     */
    public Coordinates getEffectiveCoordinates(Project project) {
        logger.debug("Calculating effective coordinates for project");
        
        var coords = project.getCoordinates();
        
        // If project has no parent, return coordinates as-is
        if (project.getParent() == null) {
            return coords;
        }
        
        // For child projects, inherit missing values from parent
        var groupId = (coords.getGroupId() == null || coords.getGroupId().getValue().isEmpty()) 
                ? new io.joshuasalcedo.fx.domain.value.GroupId(project.getParent().coordinates().getGroupId().getValue() + "." + coords.getArtifactId())
                : coords.getGroupId();
                
        var version = (coords.getVersion() == null || coords.getVersion().getValue().isEmpty())
                ? new Version(project.getParent().coordinates().getVersion().getValue())
                : coords.getVersion();
        
        return new Coordinates(groupId, coords.getArtifactId(), version);
    }

    /**
     * Calculates project hierarchy depth
     */
    public int calculateHierarchyDepth(Project project) {
        int depth = 0;
        Project current = project.getParent() != null ? findParentProject(project) : null;
        
        while (current != null) {
            depth++;
            current = current.getParent() != null ? findParentProject(current) : null;
        }
        
        return depth;
    }

    private Project findParentProject(Project project) {
        // This would typically use a repository to find the parent project
        // For now, return null as this requires infrastructure implementation
        return null;
    }

    /**
     * Result class for project validation
     */
    public static class ProjectValidationResult {
        private final List<String> errors = new java.util.ArrayList<>();
        private final List<String> warnings = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public List<String> getErrors() {
            return List.copyOf(errors);
        }
        
        public List<String> getWarnings() {
            return List.copyOf(warnings);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    /**
     * Result class for dependency conversion
     */
    public static class ConversionResult {
        private final List<Dependency> convertedDependencies = new java.util.ArrayList<>();
        private final List<Dependency> unchangedDependencies = new java.util.ArrayList<>();
        private final Map<String, String> versionProperties = new java.util.HashMap<>();
        
        public void addConvertedDependency(Dependency dependency) {
            convertedDependencies.add(dependency);
        }
        
        public void addUnchangedDependency(Dependency dependency) {
            unchangedDependencies.add(dependency);
        }
        
        public List<Dependency> getConvertedDependencies() {
            return List.copyOf(convertedDependencies);
        }
        
        public List<Dependency> getUnchangedDependencies() {
            return List.copyOf(unchangedDependencies);
        }
        
        public List<Dependency> getAllDependencies() {
            var all = new java.util.ArrayList<Dependency>();
            all.addAll(convertedDependencies);
            all.addAll(unchangedDependencies);
            return all;
        }
        
        public Map<String, String> getVersionProperties() {
            return versionProperties;
        }
    }
}