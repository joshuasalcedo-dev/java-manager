package io.joshuasalcedo.fx.domain.model.project;


import io.joshuasalcedo.fx.domain.value.ArtifactId;
import io.joshuasalcedo.fx.domain.value.GroupId;
import io.joshuasalcedo.fx.domain.value.Scope;
import io.joshuasalcedo.fx.domain.value.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Dependency class.
 *
 * @author JoshuaSalcedo
 * @created 7/24/2025 10:51 PM
 * @since ${PROJECT.version}
 */
public class Dependency {

    Logger logger = LoggerFactory.getLogger(Dependency.class);
    private GroupId groupId;
    private ArtifactId artifactId;
    Optional<Version> version;
    Optional<Scope> scope;


    public Dependency(GroupId groupId, ArtifactId artifactId, Version version, Scope scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = Optional.ofNullable(version);
        this.scope = Optional.ofNullable(scope);
    }

    public Dependency(GroupId groupId, ArtifactId artifactId, Version version) {
        this(groupId, artifactId, version, null);
    }
    public Dependency(GroupId groupId, ArtifactId artifactId) {
        this(groupId, artifactId, null, null);
    }

    public Dependency(String groupId, String artifactId, String version) {
        this(new GroupId(groupId), new ArtifactId(artifactId), new Version(version));
    }
    public Dependency(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }


    public GroupId getGroupId() {
        return groupId;
    }

    public ArtifactId getArtifactId() {
        return artifactId;
    }

    public Optional<Version> getVersion() {
        return version;
    }

    public Optional<Scope> getScope() {
        return scope;
    }

    public String versionPropertyKey() {
        return String.format("%s:%s", groupId.getValue(), artifactId.getValue());
    }

    public String toGAV() {
        return String.format("%s:%s:%s", groupId.getValue(), artifactId.getValue(), version.orElse(new Version("")).getValue());
    }

    public String toGA() {
        return String.format("%s:%s", groupId.getValue(), artifactId.getValue());
    }

    public Dependency toVersionPropertyDependency(Map<String, String> versionProperties) {
        if(this.version.orElseThrow().isProperty()){
            logger.warn("Dependency {} is already a version property", this.toGAV());
            return this;
        }
        logger.debug("Dependency {} is not a version property, converting to version property dependency", this.toGAV());

        final Version originalVersion = version.orElse(new Version(""));
        versionProperties.put(versionPropertyKey(), originalVersion.getValue());

        Version propertyVersion = new Version("${" + versionPropertyKey() + "}");
        return new Dependency(groupId, artifactId, propertyVersion, scope.orElse(null));
    }

    // Business methods
    
    public boolean isTestDependency() {
        return scope.isPresent() && "test".equalsIgnoreCase(scope.get().getValue());
    }
    
    public boolean isCompileDependency() {
        return scope.isEmpty() || "compile".equalsIgnoreCase(scope.get().getValue());
    }
    
    public boolean isRuntimeDependency() {
        return scope.isPresent() && "runtime".equalsIgnoreCase(scope.get().getValue());
    }
    
    public boolean isProvidedDependency() {
        return scope.isPresent() && "provided".equalsIgnoreCase(scope.get().getValue());
    }
    
    public boolean hasExplicitVersion() {
        return version.isPresent() && !version.get().getValue().isEmpty();
    }
    
    public boolean usesPropertyVersion() {
        return version.isPresent() && version.get().isProperty();
    }
    
    public boolean isSnapshot() {
        return version.isPresent() && version.get().isSnapshot();
    }
    
    public boolean isRelease() {
        return version.isPresent() && version.get().isRelease();
    }
    
    public boolean needsVersionResolution() {
        return version.isEmpty() || version.get().isProperty();
    }
    
    public boolean canBeConvertedToVersionProperty() {
        return hasExplicitVersion() && !usesPropertyVersion();
    }
    
    public boolean isSameGA(Dependency other) {
        if (other == null) return false;
        return groupId.equals(other.groupId) && artifactId.equals(other.artifactId);
    }
    
    public boolean hasSameCoordinates(Dependency other) {
        if (other == null) return false;
        return isSameGA(other) && 
               java.util.Objects.equals(version, other.version);
    }
    
    /**
     * Creates a new dependency with a different version
     */
    public Dependency withVersion(Version newVersion) {
        return new Dependency(groupId, artifactId, newVersion, scope.orElse(null));
    }
    
    /**
     * Creates a new dependency with a different scope
     */
    public Dependency withScope(Scope newScope) {
        return new Dependency(groupId, artifactId, version.orElse(null), newScope);
    }
    
    /**
     * Creates a new dependency without scope (compile scope)
     */
    public Dependency withoutScope() {
        return new Dependency(groupId, artifactId, version.orElse(null), null);
    }
    
    /**
     * Creates a test scoped version of this dependency
     */
    public Dependency asTestDependency() {
        return withScope(new Scope("test"));
    }
    
    /**
     * Creates a provided scoped version of this dependency
     */
    public Dependency asProvidedDependency() {
        return withScope(new Scope("provided"));
    }
    
    /**
     * Validates the dependency according to Maven business rules
     */
    public boolean isValid() {
        // Must have groupId and artifactId
        if (groupId == null || groupId.getValue().isEmpty()) {
            return false;
        }
        if (artifactId == null || artifactId.getValue().isEmpty()) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Dependency other)) return false;
        return java.util.Objects.equals(groupId, other.groupId) &&
               java.util.Objects.equals(artifactId, other.artifactId) &&
               java.util.Objects.equals(version, other.version) &&
               java.util.Objects.equals(scope, other.scope);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(groupId, artifactId, version, scope);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dependency[");
        sb.append(toGAV());
        if (scope.isPresent()) {
            sb.append(", scope=").append(scope.get().getValue());
        }
        sb.append("]");
        return sb.toString();
    }
}