package io.joshuasalcedo.fx.domain.model.project;

/**
 * ProjectPathNullException annotation.
 *
 * @author JoshuaSalcedo
 * @created 7/24/2025 11:49 PM
 * @since ${PROJECT.version}
 */
public class ProjectPathNullException extends RuntimeException {
    public ProjectPathNullException(String message) {
        super(message);
    }

    public ProjectPathNullException() {
        super("Project path cannot be null");
    }
}
