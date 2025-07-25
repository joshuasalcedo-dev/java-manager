package io.joshuasalcedo.fx.domain.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AggregateRoot class.
 *
 * @author JoshuaSalcedo
 * @created 7/24/2025 11:07 PM
 * @since ${PROJECT.version}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AggregateRoot {
}