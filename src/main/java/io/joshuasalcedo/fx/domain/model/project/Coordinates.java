package io.joshuasalcedo.fx.domain.model.project;

import io.joshuasalcedo.fx.domain.value.ArtifactId;
import io.joshuasalcedo.fx.domain.value.GroupId;
import io.joshuasalcedo.fx.domain.value.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Coordinates class representing complete Maven coordinates (GAV).
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025 2:38 AM
 * @since ${PROJECT.version}
 */
public final class Coordinates {
    private static final Logger logger = LoggerFactory.getLogger(Coordinates.class);

    private final Version version;
    private final GroupId groupId;
    private final ArtifactId artifactId;

    public Coordinates(GroupId groupId, ArtifactId artifactId, Version version) {
        this.groupId = groupId;
        this.artifactId = Objects.requireNonNull(artifactId, "ArtifactId cannot be null");
        this.version = version;
        logger.debug("Created Coordinates: {}:{}:{}",
                groupId != null ? groupId.getValue() : "",
                artifactId.getValue(),
                version != null ? version.getValue() : "");
    }

    public Coordinates(String groupId, String artifactId, String version) {
        this(
                groupId != null ? new GroupId(groupId) : new GroupId(""),
                new ArtifactId(artifactId),
                version != null ? new Version(version) : null
        );
    }

    public GroupId getGroupId() {
        logger.debug("Getting groupId: {}", groupId);
        return groupId;
    }

    public ArtifactId getArtifactId() {
        logger.debug("Getting artifactId: {}", artifactId);
        return artifactId;
    }

    public Version getVersion() {
        logger.debug("Getting version: {}", version);
        if (version == null) {
            // Return a Version object with an empty string
            return new Version("");
        }
        return version;
    }

    public String toGAV() {
        if (groupId == null) {
            throw new IllegalStateException("GroupId is required for GAV format");
        }
        String versionValue = version == null ? "" : version.getValue();
        return String.format("%s:%s:%s", groupId.getValue(), artifactId.getValue(), versionValue);
    }

    public String toGA() {
        if (groupId == null) {
            throw new IllegalStateException("GroupId is required for GA format");
        }
        return String.format("%s:%s", groupId.getValue(), artifactId.getValue());
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s",
                groupId != null ? groupId.getValue() : "",
                artifactId.getValue(),
                version != null ? version.getValue() : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinates other)) return false;
        return Objects.equals(groupId, other.groupId) &&
               Objects.equals(artifactId, other.artifactId) &&
               Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    public String toXmlString(){
        return String.format("""
                %s
                <artifactId>%s</artifactId>
                %s


                """, groupId.getValue() == null ?
                    "<!-- Empty GroupdID -->" : String.format("<groupId>%s</groupId>",groupId.getValue())
                , artifactId.getValue(),
                version == null || version.getValue() == null || version.getValue().isEmpty() ?
                "<!-- Empty Version -->" : String.format("<version>%s</version>",version.getValue())
                );
    }


}
