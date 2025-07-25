package io.joshuasalcedo.fx.domain.maven;

import io.joshuasalcedo.fx.domain.model.project.Coordinates;
import io.joshuasalcedo.fx.domain.value.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {

    private Logger logger = LoggerFactory.getLogger(CoordinatesTest.class);
    private Coordinates coordinates;
    private Coordinates coordinatesWithoutVersion;
    private Coordinates snapshotCoordinates;
    private Coordinates releaseCoordinates;
    @BeforeEach
    public void setUp() {

        this.coordinates = new Coordinates("io.joshuasalcedo", "springboot","1.0.0");
        this.coordinatesWithoutVersion = new Coordinates(coordinates.getGroupId(), coordinates.getArtifactId(), null);
        this.snapshotCoordinates = new Coordinates(coordinates.getGroupId(), coordinates.getArtifactId(), new Version(coordinates.getVersion().getValue() + "-SNAPSHOT"));
        this.releaseCoordinates = new Coordinates(coordinates.getGroupId(), coordinates.getArtifactId(), new Version(coordinates.getVersion().getValue()));
    }

    @Test
    void getGroupId() {
        assertDoesNotThrow(()->{
            assertEquals("io.joshuasalcedo", coordinates.getGroupId().getValue());
            log(coordinates);



            assertEquals("io.joshuasalcedo", snapshotCoordinates.getGroupId().getValue());
            log(snapshotCoordinates);
            assertEquals("io.joshuasalcedo", releaseCoordinates.getGroupId().getValue());
            log(releaseCoordinates);
        });

        assertEquals("", coordinatesWithoutVersion.getVersion().getValue());
        log(coordinatesWithoutVersion);
    }

    private void log(Coordinates coordinates){
        logger.info("Coordinates: {}", coordinates);
        logger.info("XML {}", coordinates.toXmlString());
        logger.info("GA {}", coordinates.toGA());
        logger.info("GAV {}", coordinates.toGAV());
    }

    @Test
    void getArtifactId() {
    }

    @Test
    void getVersion() {
    }

    @Test
    void toGAV() {
    }

    @Test
    void toGA() {
    }

    @Test
    void testToString() {
    }
}
