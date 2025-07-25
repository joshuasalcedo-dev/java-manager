package io.joshuasalcedo.fx.domain.repository;

import io.joshuasalcedo.fx.domain.model.project.Coordinates;
import io.joshuasalcedo.fx.domain.model.project.Project;
import io.joshuasalcedo.fx.domain.spec.Specification;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Maven Projects following DDD patterns.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public interface ProjectRepository {

    /**
     * Find a project by its coordinates
     */
    Optional<Project> findByCoordinates(Coordinates coordinates);

    /**
     * Find a project by its root path
     */
    Optional<Project> findByRootPath(Path rootPath);

    /**
     * Find projects that satisfy a specification
     */
    List<Project> findBySpecification(Specification<Project> specification);

    /**
     * Find all projects in a directory (including sub-modules)
     */
    List<Project> findAllInDirectory(Path directory);

    /**
     * Find all multi-module projects
     */
    List<Project> findMultiModuleProjects();

    /**
     * Find all snapshot projects
     */
    List<Project> findSnapshotProjects();

    /**
     * Find all release projects
     */
    List<Project> findReleaseProjects();

    /**
     * Save a project
     */
    Project save(Project project);

    /**
     * Save multiple projects
     */
    List<Project> saveAll(List<Project> projects);

    /**
     * Delete a project
     */
    void delete(Project project);

    /**
     * Check if a project exists by coordinates
     */
    boolean existsByCoordinates(Coordinates coordinates);

    /**
     * Check if a project exists at path
     */
    boolean existsAtPath(Path rootPath);

    /**
     * Count all projects
     */
    long count();

    /**
     * Count projects that satisfy a specification
     */
    long count(Specification<Project> specification);
}