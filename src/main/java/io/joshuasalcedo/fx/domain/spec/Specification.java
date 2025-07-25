package io.joshuasalcedo.fx.domain.spec;

/**
 * Specification class.
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025 5:45 AM
 * @since ${PROJECT.version}
 */
public interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
    default Specification<T> and(Specification<T> other) {
        return candidate -> isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }
    default Specification<T> or(Specification<T> other) {
        return candidate -> isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }
    default Specification<T> not() {
        return candidate -> !isSatisfiedBy(candidate);
    }
}
