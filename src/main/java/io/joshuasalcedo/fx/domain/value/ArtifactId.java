package io.joshuasalcedo.fx.domain.value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

public class ArtifactId {
        private static final Logger logger = LoggerFactory.getLogger(ArtifactId.class);
        private static final Pattern VALID_ARTIFACT_ID = Pattern.compile("^[a-zA-Z0-9._-]+$");

        private final String value;

        public ArtifactId(String artifactId) {
            if (artifactId == null || artifactId.isBlank()) {
                logger.error("ArtifactId is null or blank");
                throw new IllegalArgumentException("ArtifactId cannot be null or blank");
            }

            String trimmed = artifactId.trim();
            if (!VALID_ARTIFACT_ID.matcher(trimmed).matches()) {
                logger.error("Invalid artifactId format: {}", artifactId);
                throw new IllegalArgumentException("Invalid artifactId format: " + artifactId);
            }

            this.value = trimmed;
            logger.debug("Created ArtifactId: {}", value);
        }

        public String getValue() {
            logger.debug("Getting artifactId value: {}", value);
            return value;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ArtifactId other)) return false;
            return Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }