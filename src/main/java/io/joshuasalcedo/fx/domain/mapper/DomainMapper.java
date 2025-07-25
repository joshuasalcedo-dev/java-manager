package io.joshuasalcedo.fx.domain.mapper;

/**
 * DomainMapper class.
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025 12:41 PM
 * @since ${PROJECT.version}
 * @param <D> Domain
 * @param <E> Entity
 */
public interface DomainMapper<D,E> {

    E toEntity(D domain);
    D toDomain(E entity);

}
