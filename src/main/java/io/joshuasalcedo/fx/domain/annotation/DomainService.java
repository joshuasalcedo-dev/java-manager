package io.joshuasalcedo.fx.domain.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * DomainService annotation.
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025 12:49 PM
 * @since ${PROJECT.version}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainService {
}