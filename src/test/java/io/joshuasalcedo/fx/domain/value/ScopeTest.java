package io.joshuasalcedo.fx.domain.value;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ScopeTest {

    private Scope compileScope;
    private Scope runtimeScope;
    private Scope testScope;
    private Scope providedScope;
    private Scope systemScope;
    private Scope importScope;

    @BeforeEach
    void setUp() {
        compileScope = Scope.of("compile");
        runtimeScope = Scope.of("runtime");
        testScope = Scope.of("test");
        providedScope = Scope.of("provided");
        systemScope = Scope.of("system");
        importScope = Scope.of("import");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("compile", compileScope.getValue());
        assertEquals("runtime", runtimeScope.getValue());
        assertEquals("test", testScope.getValue());
        assertEquals("provided", providedScope.getValue());
        assertEquals("system", systemScope.getValue());
        assertEquals("import", importScope.getValue());
    }

    @Test
    void testFactoryMethods() {
        Scope scope1 = Scope.of("compile");
        assertEquals("compile", scope1.getValue());

        Scope scope2 = Scope.ofOrDefault("invalid", compileScope);
        assertEquals(compileScope, scope2);

        Scope scope3 = Scope.ofOrDefault("test", compileScope);
        assertEquals(testScope.getValue(), scope3.getValue());

        Scope scope4 = Scope.parseOrNull("runtime");
        assertNotNull(scope4);
        assertEquals("runtime", scope4.getValue());

        Scope scope5 = Scope.parseOrNull("invalid");
        assertNull(scope5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"compile", "provided", "runtime", "test", "system", "import"})
    void testValidScopes(String scopeStr) {
        assertTrue(Scope.isValid(scopeStr));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "COMPILE", "run-time", ""})
    void testInvalidScopes(String scopeStr) {
        assertFalse(Scope.isValid(scopeStr));
    }

    @Test
    void testScopeTypes() {
        assertTrue(compileScope.isCompile());
        assertFalse(compileScope.isRuntime());

        assertTrue(runtimeScope.isRuntime());
        assertFalse(runtimeScope.isCompile());

        assertTrue(testScope.isTest());
        assertFalse(testScope.isCompile());

        assertTrue(providedScope.isProvided());
        assertFalse(providedScope.isCompile());

        assertTrue(systemScope.isSystem());
        assertFalse(systemScope.isCompile());

        assertTrue(importScope.isImport());
        assertFalse(importScope.isCompile());
    }

    @Test
    void testTransitivity() {
        assertTrue(compileScope.isTransitive());
        assertTrue(runtimeScope.isTransitive());
        assertTrue(testScope.isTransitive());

        assertFalse(providedScope.isTransitive());
        assertFalse(systemScope.isTransitive());
        assertFalse(importScope.isTransitive());
    }

    @Test
    void testClasspathInclusion() {
        assertTrue(compileScope.isCompileClasspath());
        assertTrue(providedScope.isCompileClasspath());
        assertFalse(testScope.isCompileClasspath());

        assertTrue(runtimeScope.isRuntimeClasspath());
        assertFalse(providedScope.isRuntimeClasspath());

        assertTrue(testScope.isTestClasspath());
        assertTrue(compileScope.isTestClasspath());
        assertTrue(runtimeScope.isTestClasspath());
    }

    @Test
    void testRequirements() {
        assertTrue(compileScope.requiresPackaging());
        assertTrue(runtimeScope.requiresPackaging());
        assertFalse(providedScope.requiresPackaging());

        assertTrue(systemScope.requiresSystemPath());
        assertFalse(compileScope.requiresSystemPath());
    }

    @ParameterizedTest
    @CsvSource({
        "compile, compile, compile",
        "compile, runtime, runtime",
        "compile, test, test",
        "runtime, compile, compile",
        "test, compile, compile",
        "provided, compile, provided"
    })
    void testScopeInheritance(String parentScopeStr, String childScopeStr, String expectedScopeStr) {
        Scope parentScope = Scope.of(parentScopeStr);
        Scope childScope = Scope.of(childScopeStr);
        Scope resultScope = childScope.inheritFrom(parentScope);

        assertEquals(expectedScopeStr, resultScope.getValue());
    }

    @ParameterizedTest
    @CsvSource({
        "compile, compile, true",
        "compile, runtime, false",
        "compile, test, false",
        "runtime, runtime, true",
        "test, test, true",
        "test, compile, true",
        "test, runtime, true",
        "test, provided, true"
    })
    void testScopeInclusion(String requestingScopeStr, String targetScopeStr, boolean expected) {
        Scope requestingScope = Scope.of(requestingScopeStr);
        Scope targetScope = Scope.of(targetScopeStr);

        assertEquals(expected, targetScope.isIncludedIn(requestingScope));
    }

    @Test
    void testEqualsAndHashCode() {
        Scope scope1 = Scope.of("compile");
        Scope scope2 = Scope.of("compile");
        Scope scope3 = Scope.of("runtime");

        assertEquals(scope1, scope2);
        assertNotEquals(scope1, scope3);

        assertEquals(scope1.hashCode(), scope2.hashCode());
        assertNotEquals(scope1.hashCode(), scope3.hashCode());
    }

    @Test
    void testGetAllScopes() {
        var allScopes = Scope.getAllScopes();
        assertEquals(6, allScopes.size());
        assertTrue(allScopes.contains(compileScope));
        assertTrue(allScopes.contains(runtimeScope));
        assertTrue(allScopes.contains(testScope));
        assertTrue(allScopes.contains(providedScope));
        assertTrue(allScopes.contains(systemScope));
        assertTrue(allScopes.contains(importScope));
    }
}
