package io.joshuasalcedo.fx.domain.value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version value class representing Maven version identifiers.
 * 
 * Supports:
 * - Semantic versioning (major.minor.patch)
 * - Classifiers (SNAPSHOT, RELEASE, RC, etc.)
 * - Property placeholders (${property.name})
 * - Version comparison and ordering
 * 
 * This class is immutable and thread-safe.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class Version implements Comparable<Version> {
    private static final Logger logger = LoggerFactory.getLogger(Version.class);

    // Regex patterns for version parsing
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-(.+))?$"
    );
    private static final Pattern PROPERTY_PATTERN = Pattern.compile(
        "^\\$\\{([^}]+)\\}$"
    );

    // Common version constants
    public static final Version ZERO = new Version(0, 0, 0, null);
    public static final Version INITIAL = new Version(1, 0, 0, null);
    public static final Version INITIAL_SNAPSHOT = new Version(1, 0, 0, "SNAPSHOT");

    // Standard classifiers
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final String RELEASE = "RELEASE";
    public static final String FINAL = "FINAL";
    public static final String RC = "RC";
    public static final String BETA = "BETA";
    public static final String ALPHA = "ALPHA";

    private final String value;
    private final Integer major;
    private final Integer minor;
    private final Integer patch;
    private final String classifier;
    private final boolean isProperty;
    private final String propertyName;

    /**
     * Creates a Version from a string representation.
     * 
     * @param version the version string (e.g., "1.2.3", "2.0-SNAPSHOT", "${project.version}")
     * @throws IllegalArgumentException if version is null, blank, or invalid format
     */
    public Version(String version) {
        if (version == null) {
            logger.error("Version cannot be null");
            throw new IllegalArgumentException("Version cannot be null");
        }

        if (version.isBlank()) {
            logger.debug("Creating empty version");
            this.value = "";
            this.major = 0;
            this.minor = 0;
            this.patch = 0;
            this.classifier = null;
            this.isProperty = false;
            this.propertyName = null;
            return;
        }

        String trimmed = version.trim();
        this.value = trimmed;

        // Check if it's a property placeholder
        Matcher propertyMatcher = PROPERTY_PATTERN.matcher(trimmed);
        if (propertyMatcher.matches()) {
            logger.info("Version is a property placeholder: {}", trimmed);
            this.isProperty = true;
            this.propertyName = propertyMatcher.group(1);
            this.major = null;
            this.minor = null;
            this.patch = null;
            this.classifier = null;
            return;
        }

        this.isProperty = false;
        this.propertyName = null;

        // Parse semantic version
        Matcher versionMatcher = VERSION_PATTERN.matcher(trimmed);
        if (versionMatcher.matches()) {
            this.major = Integer.valueOf(versionMatcher.group(1));
            this.minor = versionMatcher.group(2) != null ? Integer.parseInt(versionMatcher.group(2)) : 0;
            this.patch = versionMatcher.group(3) != null ? Integer.parseInt(versionMatcher.group(3)) : 0;
            this.classifier = versionMatcher.group(4);
        } else {
            // Try alternative parsing for edge cases
            String[] parts = trimmed.split("-", 2);
            String versionPart = parts[0];
            String classifierPart = parts.length > 1 ? parts[1] : null;

            String[] numbers = versionPart.split("\\.");
            try {
                this.major = Integer.valueOf(numbers[0]);
                this.minor = numbers.length > 1 ? Integer.parseInt(numbers[1]) : 0;
                this.patch = numbers.length > 2 ? Integer.parseInt(numbers[2]) : 0;
                this.classifier = classifierPart;

                // Validate parsed values
                if (major < 0 || minor < 0 || patch < 0) {
                    throw new IllegalArgumentException("Version numbers cannot be negative");
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid version format: '{}' - must be numeric", version);
                throw new IllegalArgumentException("Invalid version format: " + version + " - version parts must be numeric", e);
            }
        }

        logger.debug("Parsed version '{}' as {}.{}.{}{}",
                version, major, minor, patch,
                classifier != null ? "-" + classifier : "");
    }

    /**
     * Creates a Version with specific components.
     * 
     * @param major major version (must be >= 0)
     * @param minor minor version (must be >= 0)
     * @param patch patch version (must be >= 0)
     * @param classifier optional classifier (can be null)
     * @throws IllegalArgumentException if any version number is negative
     */
    private Version(Integer major, Integer minor, Integer patch, String classifier) {
        this.major = Objects.requireNonNull(major, "Major version cannot be null");
        this.minor = Objects.requireNonNull(minor, "Minor version cannot be null");
        this.patch = Objects.requireNonNull(patch, "Patch version cannot be null");

        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version numbers cannot be negative");
        }

        this.classifier = classifier != null && !classifier.isBlank() ? classifier.trim() : null;
        this.isProperty = false;
        this.propertyName = null;
        this.value = buildVersionString();

        logger.debug("Created version: {}", value);
    }

    /**
     * Factory method to create a Version.
     */
    public static Version of(int major, int minor, int patch) {
        return new Version(major, minor, patch, null);
    }

    /**
     * Factory method to create a Version with classifier.
     */
    public static Version of(int major, int minor, int patch, String classifier) {
        return new Version(major, minor, patch, classifier);
    }

    /**
     * Parses a version string, returning null if invalid.
     */
    public static Version parseOrNull(String version) {
        try {
            return new Version(version);
        } catch (IllegalArgumentException e) {
            logger.debug("Failed to parse version: '{}'", version, e);
            return null;
        }
    }

    /**
     * Validates if a string is a valid version format.
     * Valid format is major.minor.patch[-classifier]
     */
    public static boolean isValid(String version) {
        if (version == null || version.isBlank()) {
            return false;
        }

        // Check if it's a property placeholder
        Matcher propertyMatcher = PROPERTY_PATTERN.matcher(version.trim());
        if (propertyMatcher.matches()) {
            return true;
        }

        // Must match the exact pattern for semantic versioning
        Matcher matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            return false;
        }

        // Require all three components (major.minor.patch)
        return matcher.group(1) != null && matcher.group(2) != null && matcher.group(3) != null;
    }

    private String buildVersionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append('.').append(minor).append('.').append(patch);
        if (classifier != null && !classifier.isEmpty()) {
            sb.append('-').append(classifier);
        }
        return sb.toString();
    }

    /**
     * Gets the version string value.
     * Never returns null.
     */
    public String getValue() {
        logger.debug("Getting version value: {}", value);
        return value;
    }

    /**
     * Gets the major version number.
     * Returns null for property placeholders.
     */
    public Integer getMajor() {
        logger.debug("Getting major version: {}", major);
        return major;
    }

    /**
     * Gets the minor version number.
     * Returns null for property placeholders.
     */
    public Integer getMinor() {
        logger.debug("Getting minor version: {}", minor);
        return minor;
    }

    /**
     * Gets the patch version number.
     * Returns null for property placeholders.
     */
    public Integer getPatch() {
        logger.debug("Getting patch version: {}", patch);
        return patch;
    }

    /**
     * Gets the classifier (e.g., SNAPSHOT, RC1).
     * Returns null if no classifier or if property placeholder.
     */
    public String getClassifier() {
        logger.debug("Getting classifier: {}", classifier);
        return classifier;
    }

    /**
     * Checks if this version is a property placeholder.
     */
    public boolean isProperty() {
        return isProperty;
    }

    /**
     * Gets the property name if this is a property placeholder.
     * Returns null if not a property placeholder.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Checks if this is a snapshot version.
     */
    public boolean isSnapshot() {
        return SNAPSHOT.equalsIgnoreCase(classifier);
    }

    /**
     * Checks if this is a release version (no classifier).
     */
    public boolean isRelease() {
        return !isProperty && classifier == null;
    }

    /**
     * Checks if this is a pre-release version (has non-SNAPSHOT classifier).
     */
    public boolean isPreRelease() {
        return !isProperty && classifier != null && !isSnapshot();
    }

    /**
     * Checks if this version is stable (release or specific pre-release classifiers).
     */
    public boolean isStable() {
        return isRelease() || 
               (classifier != null && (RELEASE.equalsIgnoreCase(classifier) || 
                                      FINAL.equalsIgnoreCase(classifier)));
    }

    /**
     * Creates a new Version with incremented major version.
     * @throws IllegalStateException if this is a property placeholder
     */
    public Version incrementMajor() {
        if (isProperty) {
            throw new IllegalStateException("Cannot increment version for property placeholder: " + value);
        }
        logger.debug("Incrementing major version from {} to {}", major, major + 1);
        return new Version(major + 1, 0, 0, classifier);
    }

    /**
     * Creates a new Version with incremented minor version.
     * @throws IllegalStateException if this is a property placeholder
     */
    public Version incrementMinor() {
        if (isProperty) {
            throw new IllegalStateException("Cannot increment version for property placeholder: " + value);
        }
        logger.debug("Incrementing minor version from {} to {}", minor, minor + 1);
        return new Version(major, minor + 1, 0, classifier);
    }

    /**
     * Creates a new Version with incremented patch version.
     * @throws IllegalStateException if this is a property placeholder
     */
    public Version incrementPatch() {
        if (isProperty) {
            throw new IllegalStateException("Cannot increment version for property placeholder: " + value);
        }
        logger.debug("Incrementing patch version from {} to {}", patch, patch + 1);
        return new Version(major, minor, patch + 1, classifier);
    }

    /**
     * Creates a release version (removes classifier).
     * @throws IllegalStateException if this is a property placeholder
     */
    public Version toRelease() {
        if (isProperty) {
            throw new IllegalStateException("Cannot convert property placeholder to release: " + value);
        }
        if (isRelease()) {
            return this;
        }
        logger.debug("Converting to release version");
        return new Version(major, minor, patch, null);
    }

    /**
     * Creates a snapshot version.
     * @throws IllegalStateException if this is a property placeholder
     */
    public Version toSnapshot() {
        if (isProperty) {
            throw new IllegalStateException("Cannot convert property placeholder to snapshot: " + value);
        }
        if (isSnapshot()) {
            return this;
        }
        logger.debug("Converting to snapshot version");
        return new Version(major, minor, patch, SNAPSHOT);
    }

    /**
     * Creates a new version with the specified classifier.
     * @throws IllegalStateException if this is a property placeholder
     */
    public Version withClassifier(String newClassifier) {
        if (isProperty) {
            throw new IllegalStateException("Cannot set classifier for property placeholder: " + value);
        }
        logger.debug("Setting classifier to: {}", newClassifier);
        return new Version(major, minor, patch, newClassifier);
    }

    /**
     * Compares versions for ordering.
     * Property placeholders are considered less than any concrete version.
     */
    @Override
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        }

        // Property placeholders come first
        if (this.isProperty && other.isProperty) {
            return this.value.compareTo(other.value);
        }
        if (this.isProperty) {
            return -1;
        }
        if (other.isProperty) {
            return 1;
        }

        // Compare version numbers
        int result = this.major.compareTo(other.major);
        if (result != 0) return result;

        result = this.minor.compareTo(other.minor);
        if (result != 0) return result;

        result = this.patch.compareTo(other.patch);
        if (result != 0) return result;

        // Compare classifiers (null = release comes after classified versions)
        if (this.classifier == null && other.classifier == null) {
            return 0;
        }
        if (this.classifier == null) {
            return 1; // Release versions are "greater" than pre-release
        }
        if (other.classifier == null) {
            return -1;
        }

        // Special handling for common classifiers
        return compareClassifiers(this.classifier, other.classifier);
    }

    private static int compareClassifiers(String c1, String c2) {
        // Define ordering for common classifiers
        int priority1 = getClassifierPriority(c1);
        int priority2 = getClassifierPriority(c2);

        if (priority1 != priority2) {
            return Integer.compare(priority1, priority2);
        }

        // Same priority level, compare lexicographically
        return c1.compareToIgnoreCase(c2);
    }

    private static int getClassifierPriority(String classifier) {
        String upper = classifier.toUpperCase();
        if (upper.startsWith(ALPHA)) return 1;
        if (upper.startsWith(BETA)) return 2;
        if (upper.startsWith(RC)) return 3;
        if (upper.equals(SNAPSHOT)) return 4;
        if (upper.equals(RELEASE) || upper.equals(FINAL)) return 5;
        return 10; // Unknown classifiers
    }

    /**
     * Checks if this version is newer than another.
     */
    public boolean isNewerThan(Version other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this version is older than another.
     */
    public boolean isOlderThan(Version other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this version is compatible with another (same major version).
     */
    public boolean isCompatibleWith(Version other) {
        if (other == null || this.isProperty || other.isProperty) {
            return false;
        }
        return Objects.equals(this.major, other.major);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Version other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Fluent API for version updates
     */
    public static class VersionChanger {
        private final Version original;
        private Integer major;
        private Integer minor;
        private Integer patch;
        private String classifier;

        private VersionChanger(Version version) {
            if (version == null) {
                throw new IllegalArgumentException("Version cannot be null");
            }
            if (version.isProperty()) {
                throw new IllegalArgumentException("Cannot change property placeholder versions: " + version.value);
            }
            this.original = version;
            this.major = version.major;
            this.minor = version.minor;
            this.patch = version.patch;
            this.classifier = version.classifier;
        }

        public static VersionChanger of(Version version) {
            return new VersionChanger(version);
        }

        public UpdateBuilder update() {
            return new UpdateBuilder();
        }

        public VersionChanger reset() {
            this.major = original.major;
            this.minor = original.minor;
            this.patch = original.patch;
            this.classifier = original.classifier;
            return this;
        }

        public class UpdateBuilder {
            public VersionChanger major() {
                logger.debug("Updating major version from {} to {}", original.major, original.major + 1);
                major = original.major + 1;
                minor = 0;
                patch = 0;
                return VersionChanger.this;
            }

            public VersionChanger minor() {
                logger.debug("Updating minor version from {} to {}", original.minor, original.minor + 1);
                major = original.major;
                minor = original.minor + 1;
                patch = 0;
                return VersionChanger.this;
            }

            public VersionChanger patch() {
                logger.debug("Updating patch version from {} to {}", original.patch, original.patch + 1);
                major = original.major;
                minor = original.minor;
                patch = original.patch + 1;
                return VersionChanger.this;
            }

            public VersionChanger to(int major, int minor, int patch) {
                logger.debug("Setting version to {}.{}.{}", major, minor, patch);
                VersionChanger.this.major = major;
                VersionChanger.this.minor = minor;
                VersionChanger.this.patch = patch;
                return VersionChanger.this;
            }
        }

        public Version toRelease() {
            logger.debug("Converting to release version: {}.{}.{}", major, minor, patch);
            return new Version(major, minor, patch, null);
        }

        public Version forSnapshot() {
            logger.debug("Converting to snapshot version: {}.{}.{}-SNAPSHOT", major, minor, patch);
            return new Version(major, minor, patch, SNAPSHOT);
        }

        public Version withClassifier(String classifier) {
            logger.debug("Setting classifier to: {}", classifier);
            return new Version(original.major, original.minor, original.patch, classifier);
        }

        public Version build() {
            return new Version(major, minor, patch, classifier);
        }
    }
}
