package io.joshuasalcedo.fx.domain.repository;

import io.joshuasalcedo.fx.domain.model.project.Dependency;
import io.joshuasalcedo.fx.domain.spec.Specification;
import io.joshuasalcedo.fx.domain.value.GroupId;

import java.util.List;

/**
 * Repository interface for Maven Dependencies following DDD patterns.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public interface DependencyRepository {

    /**
     * Find dependencies by coordinates (GA)
     */
    List<Dependency> findByCoordinates(GroupId groupId, io.joshuasalcedo.fx.domain.value.ArtifactId artifactId);

    /**
     * Find dependencies that satisfy a specification
     */
    List<Dependency> findBySpecification(Specification<Dependency> specification);

    /**
     * Find all snapshot dependencies
     */
    List<Dependency> findSnapshotDependencies();

    /**
     * Find all release dependencies
     */
    List<Dependency> findReleaseDependencies();

    /**
     * Find all dependencies using property versions
     */
    List<Dependency> findDependenciesWithPropertyVersions();

    /**
     * Find all test dependencies
     */
    List<Dependency> findTestDependencies();

    /**
     * Find all compile dependencies
     */
    List<Dependency> findCompileDependencies();

    /**
     * Find dependencies by group id
     */
    List<Dependency> findByGroupId(GroupId groupId);

    /**
     * Find dependencies that need version resolution
     */
    List<Dependency> findDependenciesNeedingVersionResolution();

    /**
     * Save a dependency
     */
    Dependency save(Dependency dependency);

    /**
     * Save multiple dependencies
     */
    List<Dependency> saveAll(List<Dependency> dependencies);

    /**
     * Delete a dependency
     */
    void delete(Dependency dependency);

    /**
     * Check if a dependency exists
     */
    boolean exists(GroupId groupId, io.joshuasalcedo.fx.domain.value.ArtifactId artifactId);

    /**
     * Count all dependencies
     */
    long count();

    /**
     * Count dependencies that satisfy a specification
     */
    long count(Specification<Dependency> specification);
}