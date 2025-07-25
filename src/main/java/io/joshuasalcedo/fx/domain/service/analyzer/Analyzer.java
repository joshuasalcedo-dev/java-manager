package io.joshuasalcedo.fx.domain.service.analyzer;

/**
 * Analyzer class.
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025 4:12 PM
 * @since ${PROJECT.version}
 */
public interface Analyzer<R,T> {
    String getName();
    String getDescription();
    R analyze(T t);
}
