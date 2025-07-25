package io.joshuasalcedo.fx.domain.service;

import io.joshuasalcedo.fx.domain.model.project.Dependency;
import io.joshuasalcedo.fx.domain.spec.maven.DependencySpecifications;
import io.joshuasalcedo.fx.domain.value.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Domain service for complex Maven Dependency business logic.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public class DependencyDomainService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyDomainService.class);

    /**
     * Validates a dependency according to Maven business rules
     */
    public DependencyValidationResult validateDependency(Dependency dependency) {
        logger.debug("Validating dependency: {}", dependency.toGA());
        
        var result = new DependencyValidationResult();
        
        // Check required coordinates
        if (!DependencySpecifications.hasRequiredCoordinates().isSatisfiedBy(dependency)) {
            result.addError("Dependency must have groupId and artifactId");
        }
        
        // Check if it's a valid Maven dependency
        if (!DependencySpecifications.isValidMavenDependency().isSatisfiedBy(dependency)) {
            result.addError("Dependency does not meet Maven requirements");
        }
        
        // Warnings for missing version
        if (!DependencySpecifications.hasExplicitVersion().isSatisfiedBy(dependency)) {
            result.addWarning("Dependency has no explicit version - may inherit from parent or dependencyManagement");
        }
        
        logger.debug("Dependency validation completed with {} errors, {} warnings", 
                result.getErrors().size(), result.getWarnings().size());
        
        return result;
    }

    /**
     * Analyzes dependency conflicts in a list of dependencies
     */
    public ConflictAnalysisResult analyzeConflicts(List<Dependency> dependencies) {
        logger.debug("Analyzing conflicts in {} dependencies", dependencies.size());
        
        var result = new ConflictAnalysisResult();
        Map<String, List<Dependency>> groupedByGA = new java.util.HashMap<>();
        
        // Group dependencies by GA coordinates
        for (Dependency dependency : dependencies) {
            String ga = dependency.toGA();
            groupedByGA.computeIfAbsent(ga, k -> new java.util.ArrayList<>()).add(dependency);
        }
        
        // Find conflicts (same GA, different versions)
        for (Map.Entry<String, List<Dependency>> entry : groupedByGA.entrySet()) {
            List<Dependency> deps = entry.getValue();
            if (deps.size() > 1) {
                // Check if they have different versions
                boolean hasConflict = deps.stream()
                        .filter(d -> d.getVersion().isPresent())
                        .map(d -> d.getVersion().get().getValue())
                        .distinct()
                        .count() > 1;
                
                if (hasConflict) {
                    result.addConflict(new DependencyConflict(entry.getKey(), deps));
                    logger.debug("Found version conflict for {}: {} versions", entry.getKey(), deps.size());
                }
            }
        }
        
        logger.debug("Conflict analysis completed: {} conflicts found", result.getConflicts().size());
        return result;
    }

    /**
     * Suggests version resolution strategies for dependencies
     */
    public VersionResolutionSuggestion suggestVersionResolution(List<Dependency> conflictingDependencies, 
                                                               Map<String, String> availableProperties) {
        logger.debug("Suggesting version resolution for {} conflicting dependencies", 
                conflictingDependencies.size());
        
        var suggestion = new VersionResolutionSuggestion();
        
        // Find the highest version
        Optional<Version> highestVersion = conflictingDependencies.stream()
                .filter(d -> d.getVersion().isPresent())
                .map(d -> d.getVersion().get())
                .filter(v -> !v.isProperty())
                .max(Version::compareTo);
        
        if (highestVersion.isPresent()) {
            suggestion.setRecommendedVersion(highestVersion.get());
            suggestion.setStrategy(ResolutionStrategy.USE_HIGHEST_VERSION);
            logger.debug("Recommended version: {} (highest version strategy)", highestVersion.get());
        } else {
            // If no concrete versions available, suggest using a property
            suggestion.setStrategy(ResolutionStrategy.USE_PROPERTY);
            logger.debug("Recommended strategy: use version property");
        }
        
        return suggestion;
    }

    /**
     * Filters dependencies by scope and lifecycle phase
     */
    public DependencyFilterResult filterDependencies(List<Dependency> dependencies, DependencyFilter filter) {
        logger.debug("Filtering {} dependencies with filter: {}", dependencies.size(), filter);
        
        var result = new DependencyFilterResult();
        
        for (Dependency dependency : dependencies) {
            if (filter.accept(dependency)) {
                result.addAcceptedDependency(dependency);
            } else {
                result.addRejectedDependency(dependency);
            }
        }
        
        logger.debug("Filtering completed: {} accepted, {} rejected", 
                result.getAcceptedDependencies().size(), 
                result.getRejectedDependencies().size());
        
        return result;
    }

    /**
     * Creates a dependency exclusion recommendation
     */
    public ExclusionRecommendation recommendExclusions(List<Dependency> dependencies, 
                                                      List<String> unwantedGroupIds) {
        logger.debug("Analyzing exclusion recommendations for {} dependencies", dependencies.size());
        
        var recommendation = new ExclusionRecommendation();
        
        for (Dependency dependency : dependencies) {
            String groupId = dependency.getGroupId().getValue();
            if (unwantedGroupIds.contains(groupId)) {
                recommendation.addExclusion(dependency, "Unwanted groupId: " + groupId);
                logger.debug("Recommended exclusion for dependency: {} (unwanted groupId)", dependency.toGA());
            }
        }
        
        return recommendation;
    }

    // Helper classes and interfaces

    public interface DependencyFilter {
        boolean accept(Dependency dependency);
        
        static DependencyFilter testScope() {
            return DependencySpecifications.isTestDependency()::isSatisfiedBy;
        }
        
        static DependencyFilter compileScope() {
            return DependencySpecifications.isCompileDependency()::isSatisfiedBy;
        }
        
        static DependencyFilter snapshotVersions() {
            return DependencySpecifications.isSnapshotDependency()::isSatisfiedBy;
        }
        
        static DependencyFilter releaseVersions() {
            return DependencySpecifications.isReleaseDependency()::isSatisfiedBy;
        }
    }

    public enum ResolutionStrategy {
        USE_HIGHEST_VERSION,
        USE_LOWEST_VERSION,
        USE_PROPERTY,
        MANUAL_RESOLUTION
    }

    // Result classes
    public static class DependencyValidationResult {
        private final List<String> errors = new java.util.ArrayList<>();
        private final List<String> warnings = new java.util.ArrayList<>();
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public List<String> getErrors() { return List.copyOf(errors); }
        public List<String> getWarnings() { return List.copyOf(warnings); }
        public boolean isValid() { return errors.isEmpty(); }
    }

    public static class ConflictAnalysisResult {
        private final List<DependencyConflict> conflicts = new java.util.ArrayList<>();
        
        public void addConflict(DependencyConflict conflict) { conflicts.add(conflict); }
        public List<DependencyConflict> getConflicts() { return List.copyOf(conflicts); }
        public boolean hasConflicts() { return !conflicts.isEmpty(); }
    }

    public static class DependencyConflict {
        private final String ga;
        private final List<Dependency> conflictingDependencies;
        
        public DependencyConflict(String ga, List<Dependency> conflictingDependencies) {
            this.ga = ga;
            this.conflictingDependencies = List.copyOf(conflictingDependencies);
        }
        
        public String getGa() { return ga; }
        public List<Dependency> getConflictingDependencies() { return conflictingDependencies; }
    }

    public static class VersionResolutionSuggestion {
        private Version recommendedVersion;
        private ResolutionStrategy strategy;
        private String reason;
        
        public Version getRecommendedVersion() { return recommendedVersion; }
        public void setRecommendedVersion(Version recommendedVersion) { this.recommendedVersion = recommendedVersion; }
        public ResolutionStrategy getStrategy() { return strategy; }
        public void setStrategy(ResolutionStrategy strategy) { this.strategy = strategy; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class DependencyFilterResult {
        private final List<Dependency> acceptedDependencies = new java.util.ArrayList<>();
        private final List<Dependency> rejectedDependencies = new java.util.ArrayList<>();
        
        public void addAcceptedDependency(Dependency dependency) { acceptedDependencies.add(dependency); }
        public void addRejectedDependency(Dependency dependency) { rejectedDependencies.add(dependency); }
        public List<Dependency> getAcceptedDependencies() { return List.copyOf(acceptedDependencies); }
        public List<Dependency> getRejectedDependencies() { return List.copyOf(rejectedDependencies); }
    }

    public static class ExclusionRecommendation {
        private final Map<Dependency, String> exclusions = new java.util.HashMap<>();
        
        public void addExclusion(Dependency dependency, String reason) { exclusions.put(dependency, reason); }
        public Map<Dependency, String> getExclusions() { return Map.copyOf(exclusions); }
        public boolean hasExclusions() { return !exclusions.isEmpty(); }
    }
}