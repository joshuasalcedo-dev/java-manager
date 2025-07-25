package io.joshuasalcedo.fx.domain.value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * GroupId value class representing Maven group identifier.
 * 
 * A groupId typically follows reverse domain notation:
 * - com.example (commercial organization)
 * - org.apache (open source organization)
 * - io.github.username (GitHub-based projects)
 * 
 * This class ensures that GroupId is never null and always valid.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class GroupId {
    private static final Pattern VALID_GROUP_ID = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Logger logger = LoggerFactory.getLogger(GroupId.class);
    
    // Common default groupIds
    public static final GroupId DEFAULT = new GroupId("default.group");
    public static final GroupId UNKNOWN = new GroupId("unknown.group");
    
    private final String value;
    private final List<String> segments;



    /**
     * Creates a new GroupId with validation.
     * 
     * @param groupId the group identifier string
     * @throws NullGroupIdCoordinatesException if groupId is null or blank
     * @throws IllegalArgumentException if groupId format is invalid
     */
    public GroupId(String groupId) {
        if (groupId == null) {
            logger.error("GroupId is null - this is not allowed");
            throw new NullGroupIdCoordinatesException("GroupId cannot be null");
        }
        
        if (groupId.isBlank()) {
            logger.error("GroupId is blank - this is not allowed");
            throw new NullGroupIdCoordinatesException("GroupId cannot be blank");
        }

        String trimmed = groupId.trim();
        
        if (!VALID_GROUP_ID.matcher(trimmed).matches()) {
            logger.error("Invalid groupId format: '{}' - must contain only letters, numbers, dots, hyphens, and underscores", groupId);
            throw new IllegalArgumentException("Invalid groupId format: " + groupId);
        }

        this.segments = Arrays.asList(trimmed.split("\\."));

        if (segments.isEmpty()) {
            logger.error("GroupId resulted in empty segments: '{}'", groupId);
            throw new IllegalArgumentException("GroupId resulted in empty segments: " + groupId);
        }
        
        if (segments.stream().anyMatch(String::isEmpty)) {
            logger.error("GroupId contains empty segments: '{}' - consecutive dots are not allowed", groupId);
            throw new IllegalArgumentException("GroupId contains empty segments (consecutive dots): " + groupId);
        }
        
        // Validate each segment
        for (String segment : segments) {
            if (!isValidSegment(segment)) {
                logger.error("Invalid segment '{}' in groupId '{}'", segment, groupId);
                throw new IllegalArgumentException("Invalid segment '" + segment + "' in groupId: " + groupId);
            }
        }

        this.value = trimmed;
        logger.debug("Successfully created GroupId: '{}' with {} segments", value, segments.size());
    }
    
    /**
     * Validates a single segment of the groupId.
     */
    private static boolean isValidSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            return false;
        }
        // Segments should not start or end with special characters
        if (segment.startsWith("-") || segment.startsWith("_") || 
            segment.endsWith("-") || segment.endsWith("_")) {
            return false;
        }
        return true;
    }

    /**
     * Factory method to create GroupId from segments.
     * 
     * @param segments the segments to join
     * @return a new GroupId
     * @throws IllegalArgumentException if segments are null, empty, or invalid
     */
    public static GroupId of(String... segments) {
        if (segments == null) {
            logger.error("Cannot create GroupId from null segments");
            throw new IllegalArgumentException("Segments cannot be null");
        }
        
        if (segments.length == 0) {
            logger.error("Cannot create GroupId from empty segments");
            throw new IllegalArgumentException("At least one segment is required");
        }
        
        // Validate each segment before joining
        for (int i = 0; i < segments.length; i++) {
            if (segments[i] == null) {
                logger.error("Segment at index {} is null", i);
                throw new IllegalArgumentException("Segment at index " + i + " is null");
            }
            if (segments[i].isBlank()) {
                logger.error("Segment at index {} is blank", i);
                throw new IllegalArgumentException("Segment at index " + i + " is blank");
            }
        }
        
        return new GroupId(String.join(".", segments));
    }
    
    /**
     * Creates a GroupId or returns a default if the input is invalid.
     * 
     * @param groupId the group identifier string
     * @param defaultGroupId the default to use if groupId is invalid
     * @return a GroupId instance
     */
    public static GroupId ofOrDefault(String groupId, GroupId defaultGroupId) {
        Objects.requireNonNull(defaultGroupId, "Default GroupId cannot be null");
        
        try {
            return new GroupId(groupId);
        } catch (NullGroupIdCoordinatesException e) {
            logger.warn("Failed to create GroupId from '{}', using default: {}", groupId, defaultGroupId.value, e);
            return defaultGroupId;
        }
    }
    
    /**
     * Parses a groupId string, returning null if invalid.
     * 
     * @param groupId the group identifier string
     * @return a GroupId instance or null if invalid
     */
    public static GroupId parseOrNull(String groupId) {
        try {
            return new GroupId(groupId);
        } catch (NullGroupIdCoordinatesException e) {
            logger.debug("Failed to parse GroupId from '{}'", groupId, e);
            return null;
        }
    }
    
    /**
     * Validates if a string is a valid groupId format.
     * 
     * @param groupId the string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return false;
        }
        
        try {
            new GroupId(groupId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets the full groupId value.
     * This method never returns null.
     * 
     * @return the groupId value
     */
    public String getValue() {
        logger.debug("Getting groupId value: {}", value);
        return value;
    }

    /**
     * Gets the groupId segments as an array.
     * This method never returns null.
     * 
     * @return array of segments
     */
    public String[] getSegments() {
        String[] result = segments.toArray(new String[0]);
        if (logger.isDebugEnabled()) {
            logger.debug("Getting groupId segments: {}", Arrays.toString(result));
        }
        return result;
    }

    /**
     * Gets the groupId segments as an immutable list.
     * This method never returns null.
     * 
     * @return immutable list of segments
     */
    public List<String> getSegmentList() {
        logger.debug("Getting groupId segment list for: {}", value);
        return List.copyOf(segments);
    }

    /**
     * Gets the number of segments in the groupId.
     * 
     * @return segment count (always >= 1)
     */
    public int getSegmentCount() {
        int count = segments.size();
        logger.debug("Segment count for '{}': {}", value, count);
        return count;
    }

    /**
     * Gets the top-level domain (first segment).
     * This method never returns null.
     * 
     * @return the first segment
     */
    public String getTopLevelDomain() {
        String tld = segments.getFirst();
        logger.debug("Top-level domain for '{}': {}", value, tld);
        return tld;
    }

    /**
     * Gets the organization part.
     * For "com.example.project" returns "com.example"
     * For "com.example" returns "com.example"
     * This method never returns null.
     * 
     * @return the organization part
     */
    public String getOrganization() {
        String org;
        if (segments.size() <= 2) {
            org = value;
        } else {
            org = String.join(".", segments.subList(0, segments.size() - 1));
        }
        logger.debug("Organization for '{}': {}", value, org);
        return org;
    }

    /**
     * Converts to a valid Java package name.
     * This method never returns null.
     * 
     * @return the package name
     */
    public String toPackageName() {
        logger.debug("Converting '{}' to package name", value);
        return value;
    }

    /**
     * Converts to file system path using the specified separator.
     * This method never returns null.
     * 
     * @param separator the path separator character
     * @return the path string
     */
    public String toPath(char separator) {
        String path = value.replace('.', separator);
        logger.debug("Converted '{}' to path with separator '{}': {}", value, separator, path);
        return path;
    }

    /**
     * Converts to file system path using forward slash.
     * This method never returns null.
     * 
     * @return the path string
     */
    public String toPath() {
        return toPath('/');
    }

    /**
     * Gets the repository path for Maven layout.
     * This method never returns null.
     * 
     * @return the repository path
     */
    public String toRepositoryPath() {
        String path = toPath('/');
        logger.debug("Repository path for '{}': {}", value, path);
        return path;
    }

    /**
     * Checks if this groupId starts with another groupId.
     * 
     * @param other the other groupId (can be null)
     * @return true if starts with other, false if other is null
     */
    public boolean startsWith(GroupId other) {
        if (other == null) {
            logger.debug("Cannot check startsWith for null GroupId");
            return false;
        }
        
        boolean result = value.startsWith(other.value);
        if (logger.isDebugEnabled()) {
            logger.debug("'{}' starts with '{}': {}", value, other.value, result);
        }
        return result;
    }

    /**
     * Checks if this groupId starts with a string prefix.
     * 
     * @param prefix the prefix to check (can be null)
     * @return true if starts with prefix, false if prefix is null
     */
    public boolean startsWith(String prefix) {
        if (prefix == null) {
            logger.debug("Cannot check startsWith for null prefix");
            return false;
        }
        
        boolean result = value.startsWith(prefix);
        logger.debug("'{}' starts with '{}': {}", value, prefix, result);
        return result;
    }

    /**
     * Creates a new GroupId by appending segments.
     * This method never returns null.
     * 
     * @param additionalSegments segments to append (can be null or empty)
     * @return new GroupId or this if no segments to append
     */
    public GroupId append(String... additionalSegments) {
        if (additionalSegments == null || additionalSegments.length == 0) {
            logger.debug("No segments to append to '{}'", value);
            return this;
        }

        // Validate additional segments
        for (int i = 0; i < additionalSegments.length; i++) {
            if (additionalSegments[i] == null) {
                logger.error("Cannot append null segment at index {}", i);
                throw new IllegalArgumentException("Cannot append null segment at index " + i);
            }
            if (additionalSegments[i].isBlank()) {
                logger.error("Cannot append blank segment at index {}", i);
                throw new IllegalArgumentException("Cannot append blank segment at index " + i);
            }
        }

        String[] allSegments = new String[segments.size() + additionalSegments.length];
        System.arraycopy(getSegments(), 0, allSegments, 0, segments.size());
        System.arraycopy(additionalSegments, 0, allSegments, segments.size(), additionalSegments.length);

        GroupId newGroupId = GroupId.of(allSegments);
        logger.debug("Appended {} segments to '{}' to create '{}'", 
                    additionalSegments.length, value, newGroupId.value);
        return newGroupId;
    }

    /**
     * Gets the parent groupId by removing the last segment.
     * 
     * @return parent GroupId or null if only one segment exists
     */
    public GroupId getParent() {
        if (segments.size() <= 1) {
            logger.debug("No parent for single-segment groupId: '{}'", value);
            return null;
        }
        
        GroupId parent = new GroupId(String.join(".", segments.subList(0, segments.size() - 1)));
        logger.debug("Parent of '{}' is '{}'", value, parent.value);
        return parent;
    }
    
    /**
     * Checks if this GroupId has a parent.
     * 
     * @return true if has parent (more than one segment)
     */
    public boolean hasParent() {
        return segments.size() > 1;
    }
    
    /**
     * Gets the last segment of the groupId.
     * This method never returns null.
     * 
     * @return the last segment
     */
    public String getLastSegment() {
        String last = segments.get(segments.size() - 1);
        logger.debug("Last segment of '{}': {}", value, last);
        return last;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GroupId other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    /**
     * Compares two GroupIds lexicographically.
     * 
     * @param other the other GroupId
     * @return comparison result
     */
    public int compareTo(GroupId other) {
        if (other == null) {
            return 1;
        }
        return value.compareTo(other.value);
    }

    /**
     * Custom exception for null or blank groupId.
     */
    public static class NullGroupIdCoordinatesException extends IllegalArgumentException {
        public NullGroupIdCoordinatesException(String message) {
            super(message);
        }
        
        public NullGroupIdCoordinatesException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}