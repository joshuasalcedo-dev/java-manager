package io.joshuasalcedo.fx.domain.value;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    private Version standardVersion;
    private Version snapshotVersion;
    private Version releaseVersion;
    private Version preReleaseVersion;

    @BeforeEach
    void setUp() {
        standardVersion = new Version("1.2.3");
        snapshotVersion = new Version("1.2.3-SNAPSHOT");
        releaseVersion = new Version("1.2.3");
        preReleaseVersion = new Version("1.2.3-beta");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("1.2.3", standardVersion.getValue());
        assertEquals(Integer.valueOf(1), standardVersion.getMajor());
        assertEquals(Integer.valueOf(2), standardVersion.getMinor());
        assertEquals(Integer.valueOf(3), standardVersion.getPatch());
        assertNull(standardVersion.getClassifier());
        
        assertEquals("1.2.3-SNAPSHOT", snapshotVersion.getValue());
        assertEquals("SNAPSHOT", snapshotVersion.getClassifier());
    }

    @Test
    void testFactoryMethods() {
        Version version1 = Version.of(1, 2, 3);
        assertEquals("1.2.3", version1.getValue());
        
        Version version2 = Version.of(1, 2, 3, "alpha");
        assertEquals("1.2.3-alpha", version2.getValue());
        
        Version version3 = Version.parseOrNull("1.2.3-RC1");
        assertNotNull(version3);
        assertEquals("1.2.3-RC1", version3.getValue());
        
        Version version4 = Version.parseOrNull("invalid");
        assertNull(version4);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0.0", "10.20.30", "0.0.1", "1.0.0-SNAPSHOT", "1.0.0-RC1"})
    void testValidVersions(String versionStr) {
        assertTrue(Version.isValid(versionStr));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1.0", "a.b.c", "1.0.0.0", ""})
    void testInvalidVersions(String versionStr) {
        assertFalse(Version.isValid(versionStr));
    }

    @Test
    void testVersionTypes() {
        assertTrue(snapshotVersion.isSnapshot());
        assertFalse(snapshotVersion.isRelease());
        
        assertTrue(releaseVersion.isRelease());
        assertFalse(releaseVersion.isSnapshot());
        
        assertTrue(preReleaseVersion.isPreRelease());
        assertFalse(preReleaseVersion.isStable());
        
        assertTrue(releaseVersion.isStable());
    }

    @Test
    void testVersionIncrement() {
        Version incrementedMajor = standardVersion.incrementMajor();
        assertEquals("2.0.0", incrementedMajor.getValue());
        
        Version incrementedMinor = standardVersion.incrementMinor();
        assertEquals("1.3.0", incrementedMinor.getValue());
        
        Version incrementedPatch = standardVersion.incrementPatch();
        assertEquals("1.2.4", incrementedPatch.getValue());
    }

    @Test
    void testVersionConversion() {
        Version toRelease = snapshotVersion.toRelease();
        assertEquals("1.2.3", toRelease.getValue());
        assertTrue(toRelease.isRelease());
        
        Version toSnapshot = releaseVersion.toSnapshot();
        assertEquals("1.2.3-SNAPSHOT", toSnapshot.getValue());
        assertTrue(toSnapshot.isSnapshot());
        
        Version withNewClassifier = standardVersion.withClassifier("RC1");
        assertEquals("1.2.3-RC1", withNewClassifier.getValue());
    }

    @ParameterizedTest
    @CsvSource({
        "1.2.3, 1.2.4, -1",
        "1.2.3, 1.2.3, 0",
        "1.2.3, 1.2.2, 1",
        "1.2.3, 2.0.0, -1",
        "1.2.3-SNAPSHOT, 1.2.3, -1",
        "1.2.3-alpha, 1.2.3-beta, -1",
        "1.2.3-RC1, 1.2.3-alpha, 1"
    })
    void testVersionComparison(String version1Str, String version2Str, int expectedResult) {
        Version version1 = new Version(version1Str);
        Version version2 = new Version(version2Str);
        assertEquals(expectedResult, version1.compareTo(version2));
        
        assertEquals(expectedResult < 0, version1.isOlderThan(version2));
        assertEquals(expectedResult > 0, version1.isNewerThan(version2));
    }

    @Test
    void testVersionChanger() {
        Version.VersionChanger changer = Version.VersionChanger.of(standardVersion);
        
        Version updated1 = changer.update().major().build();
        assertEquals("2.0.0", updated1.getValue());
        
        Version updated2 = changer.update().minor().build();
        assertEquals("1.3.0", updated2.getValue());
        
        Version updated3 = changer.update().patch().build();
        assertEquals("1.2.4", updated3.getValue());
        
        Version updated4 = changer.update().to(3, 4, 5).build();
        assertEquals("3.4.5", updated4.getValue());
        
        Version updated5 = changer.forSnapshot();
        assertTrue(updated5.isSnapshot());
        
        Version updated6 = changer.toRelease();
        assertTrue(updated6.isRelease());
        
        Version updated7 = changer.withClassifier("RC2");
        assertEquals("1.2.3-RC2", updated7.getValue());
    }

    @Test
    void testEqualsAndHashCode() {
        Version version1 = new Version("1.2.3");
        Version version2 = new Version("1.2.3");
        Version version3 = new Version("1.2.4");
        
        assertEquals(version1, version2);
        assertNotEquals(version1, version3);
        
        assertEquals(version1.hashCode(), version2.hashCode());
        assertNotEquals(version1.hashCode(), version3.hashCode());
    }
}