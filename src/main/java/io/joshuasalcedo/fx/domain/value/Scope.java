package io.joshuasalcedo.fx.domain.value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Scope value class representing Maven dependency scopes.
 * 
 * Maven scopes control the classpath of a project and limit the transitivity of dependencies:
 * - COMPILE: Default scope, available in all classpaths
 * - PROVIDED: Like compile, but provided by JDK or container at runtime
 * - RUNTIME: Not required for compilation, but needed for execution
 * - TEST: Only available for test compilation and execution
 * - SYSTEM: Like provided, but you have to provide the JAR explicitly
 * - IMPORT: Only used on a dependency of type pom in the dependencyManagement section
 * 
 * This class is immutable and thread-safe.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class Scope {
    private static final Logger logger = LoggerFactory.getLogger(Scope.class);

    private static final String COMPILE_STRING = "compile";
    private static final String PROVIDED_STRING = "provided";
    private static final String RUNTIME_STRING = "runtime";
    private static final String TEST_STRING = "test";
    private static final String SYSTEM_STRING = "system";
    private static final String IMPORT_STRING= "import";

    // Standard Maven scopes
    public static final Scope COMPILE = new Scope(COMPILE_STRING);
    public static final Scope PROVIDED = new Scope(PROVIDED_STRING);
    public static final Scope RUNTIME = new Scope(RUNTIME_STRING);
    public static final Scope TEST = new Scope(TEST_STRING);
    public static final Scope SYSTEM = new Scope(SYSTEM_STRING);
    public static final Scope IMPORT = new Scope(IMPORT_STRING);

    // Common scope collections
    private static final Map<String, Scope> STANDARD_SCOPES;
    private static final Set<Scope> TRANSITIVE_SCOPES;
    private static final Set<Scope> CLASSPATH_SCOPES;
    private static final Set<Scope> TEST_CLASSPATH_SCOPES;

    static {
        STANDARD_SCOPES = Map.of(COMPILE_STRING, COMPILE, PROVIDED_STRING, PROVIDED, RUNTIME_STRING, RUNTIME, TEST_STRING, TEST, SYSTEM_STRING, SYSTEM, IMPORT_STRING, IMPORT);

        // Scopes that are transitive (inherited by dependent projects)
        TRANSITIVE_SCOPES = Set.of(COMPILE, RUNTIME, TEST);

        // Scopes available at compile time
        CLASSPATH_SCOPES = Set.of(COMPILE, PROVIDED, SYSTEM);

        // Scopes available during test execution
        TEST_CLASSPATH_SCOPES = Set.of(COMPILE, PROVIDED, RUNTIME, TEST, SYSTEM);
    }

    private final String value;

    /**
     * Private constructor to control instantiation.
     * Use factory methods or constants instead.
     */
    public Scope(String scope) {
        if (scope == null) {
            logger.error("Scope cannot be null");
            throw new IllegalArgumentException("Scope cannot be null");
        }

        if (scope.isBlank()) {
            logger.error("Scope cannot be blank");
            throw new IllegalArgumentException("Scope cannot be blank");
        }

        this.value = scope.trim().toLowerCase();
        logger.debug("Created scope: {}", this.value);
    }

    /**
     * Gets a standard Maven scope by name.
     * 
     * @param scope the scope name (case-insensitive)
     * @return the Scope instance
     * @throws IllegalArgumentException if scope is not a standard Maven scope
     */
    public static Scope of(String scope) {
        if (scope == null) {
            logger.error("Cannot get scope for null value");
            throw new IllegalArgumentException("Scope cannot be null");
        }

        String normalized = scope.trim().toLowerCase();
        Scope standardScope = STANDARD_SCOPES.get(normalized);

        if (standardScope == null) {
            logger.error("Unknown Maven scope: '{}'. Valid scopes are: {}", 
                        scope, STANDARD_SCOPES.keySet());
            throw new IllegalArgumentException(
                "Unknown Maven scope: '" + scope + "'. Valid scopes are: " + STANDARD_SCOPES.keySet()
            );
        }

        return standardScope;
    }

    /**
     * Gets a scope or returns a default if invalid.
     * 
     * @param scope the scope name
     * @param defaultScope the default to use if scope is invalid
     * @return a Scope instance
     */
    public static Scope ofOrDefault(String scope, Scope defaultScope) {
        Objects.requireNonNull(defaultScope, "Default scope cannot be null");

        try {
            return of(scope);
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid scope '{}', using default: {}", scope, defaultScope.value);
            return defaultScope;
        }
    }

    /**
     * Parses a scope string, returning null if invalid.
     * 
     * @param scope the scope name
     * @return a Scope instance or null if invalid
     */
    public static Scope parseOrNull(String scope) {
        try {
            return of(scope);
        } catch (IllegalArgumentException e) {
            logger.debug("Failed to parse scope: '{}'", scope);
            return null;
        }
    }

    /**
     * Validates if a string is a valid Maven scope.
     * 
     * @param scope the scope to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String scope) {
        if (scope == null || scope.isBlank()) {
            return false;
        }
        // Check if the scope is in the standard scopes map with exact case matching
        return STANDARD_SCOPES.containsKey(scope.trim());
    }

    /**
     * Gets all standard Maven scopes.
     * 
     * @return unmodifiable set of all standard scopes
     */
    public static Set<Scope> getAllScopes() {
        return new HashSet<>(STANDARD_SCOPES.values());
    }

    /**
     * Gets the scope value.
     * Never returns null.
     * 
     * @return the scope value in lowercase
     */
    public String getValue() {
        logger.debug("Getting scope value: {}", value);
        return value;
    }

    /**
     * Checks if this is the compile scope.
     * Compile is the default scope if none is specified.
     */
    public boolean isCompile() {
        return this == COMPILE;
    }

    /**
     * Checks if this is the provided scope.
     * Provided dependencies are available at compile time but not packaged.
     */
    public boolean isProvided() {
        return this == PROVIDED;
    }

    /**
     * Checks if this is the runtime scope.
     * Runtime dependencies are not needed for compilation but are for execution.
     */
    public boolean isRuntime() {
        return this == RUNTIME;
    }

    /**
     * Checks if this is the test scope.
     * Test dependencies are only available for test compilation and execution.
     */
    public boolean isTest() {
        return this == TEST;
    }

    /**
     * Checks if this is the system scope.
     * System scope is similar to provided but requires explicit JAR path.
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }

    /**
     * Checks if this is the import scope.
     * Import scope is only used in dependencyManagement for importing pom dependencies.
     */
    public boolean isImport() {
        return this == IMPORT;
    }

    /**
     * Checks if this scope is transitive.
     * Transitive scopes are inherited by dependent projects.
     * 
     * @return true if this scope is transitive (compile or runtime)
     */
    public boolean isTransitive() {
        boolean result = TRANSITIVE_SCOPES.contains(this);
        logger.debug("Scope '{}' is transitive: {}", value, result);
        return result;
    }

    /**
     * Checks if this scope makes the dependency available at compile time.
     * 
     * @return true if available at compile time
     */
    public boolean isCompileClasspath() {
        boolean result = CLASSPATH_SCOPES.contains(this);
        logger.debug("Scope '{}' is on compile classpath: {}", value, result);
        return result;
    }

    /**
     * Checks if this scope makes the dependency available at runtime.
     * 
     * @return true if available at runtime
     */
    public boolean isRuntimeClasspath() {
        boolean result = this == COMPILE || this == RUNTIME;
        logger.debug("Scope '{}' is on runtime classpath: {}", value, result);
        return result;
    }

    /**
     * Checks if this scope makes the dependency available during test execution.
     * 
     * @return true if available during tests
     */
    public boolean isTestClasspath() {
        boolean result = TEST_CLASSPATH_SCOPES.contains(this);
        logger.debug("Scope '{}' is on test classpath: {}", value, result);
        return result;
    }

    /**
     * Checks if this scope requires packaging the dependency.
     * 
     * @return true if the dependency should be packaged
     */
    public boolean requiresPackaging() {
        boolean result = this == COMPILE || this == RUNTIME;
        logger.debug("Scope '{}' requires packaging: {}", value, result);
        return result;
    }

    /**
     * Checks if this scope requires a system path.
     * 
     * @return true if system path is required (only for SYSTEM scope)
     */
    public boolean requiresSystemPath() {
        return this == SYSTEM;
    }

    /**
     * Gets the effective scope when this scope is inherited.
     * This implements Maven's scope inheritance rules.
     * 
     * @param parentScope the scope from the parent dependency
     * @return the effective scope after inheritance
     */
    public Scope inheritFrom(Scope parentScope) {
        if (parentScope == null) {
            logger.debug("No parent scope, returning current scope: {}", value);
            return this;
        }

        logger.debug("Inheriting scope '{}' from parent scope '{}'", value, parentScope.value);

        // Import scope is never inherited
        if (this == IMPORT || parentScope == IMPORT) {
            return this;
        }

        // Test dependencies are only available in test scope
        if (parentScope == TEST) {
            return this;
        }

        // If parent is provided, dependencies become provided
        if (parentScope == PROVIDED) {
            return PROVIDED;
        }

        // Runtime parent scope handling
        if (parentScope == RUNTIME) {
            return this;
        }

        // Default: use the current scope
        return this;
    }

    /**
     * Determines if a dependency with this scope should be included
     * given the requesting scope context.
     * 
     * @param requestingScope the scope context of the request
     * @return true if the dependency should be included
     */
    public boolean isIncludedIn(Scope requestingScope) {
        if (requestingScope == null) {
            return isTransitive();
        }

        // Test scope includes everything
        if (requestingScope == TEST) {
            return true;
        }

        // Runtime scope includes compile and runtime
        if (requestingScope == RUNTIME) {
            return this == COMPILE || this == RUNTIME;
        }

        // Compile scope only includes compile dependencies
        if (requestingScope == COMPILE) {
            return this == COMPILE;
        }

        return false;
    }

    /**
     * Gets a descriptive explanation of this scope's behavior.
     * 
     * @return human-readable description
     */
    public String getDescription() {
        return switch (this.value) {
            case COMPILE_STRING -> "Default scope. Dependencies are available in all classpaths and are transitive.";
            case PROVIDED_STRING -> "Like compile, but provided by JDK or container at runtime. Not transitive.";
            case RUNTIME_STRING -> "Not required for compilation, but needed for execution. Transitive.";
            case TEST_STRING -> "Only available for test compilation and execution. Not transitive.";
            case SYSTEM_STRING -> "Like provided, but JAR location must be specified explicitly. Not transitive.";
            case IMPORT_STRING -> "Only used in dependencyManagement to import dependency management from another POM.";
            default -> "Unknown scope: " + value;
        };
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Scope)) return false;
        Scope other = (Scope) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Compares scopes by their precedence order.
     * Order: compile < provided < runtime < test < system < import
     */
    public int compareTo(Scope other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(getPrecedence(), other.getPrecedence());
    }

    private int getPrecedence() {
        return switch (this.value) {
            case "compile" -> 1;
            case "provided" -> 2;
            case "runtime" -> 3;
            case "test" -> 4;
            case "system" -> 5;
            case "import" -> 6;
            default -> 99;
        };
    }
}
