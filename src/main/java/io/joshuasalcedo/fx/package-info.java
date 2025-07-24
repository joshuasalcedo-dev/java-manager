/**
 * This is the root package of the JavaFX application.
 * 
 * <p>This package contains the main application launcher and serves as the entry point
 * for the application. It integrates Spring Boot with JavaFX to create a modern,
 * feature-rich desktop application.</p>
 * 
 * <p>The application follows a layered architecture pattern with the following packages:
 * <ul>
 *   <li>{@link io.joshuasalcedo.fx.application} - Contains application services and business logic</li>
 *   <li>{@link io.joshuasalcedo.fx.common} - Contains common utilities and shared components</li>
 *   <li>{@link io.joshuasalcedo.fx.domain} - Contains domain model classes and business rules</li>
 *   <li>{@link io.joshuasalcedo.fx.infrastructure} - Contains technical infrastructure components</li>
 *   <li>{@link io.joshuasalcedo.fx.presentation} - Contains UI components and controllers</li>
 * </ul>
 * </p>
 * 
 * <p>The main class {@link io.joshuasalcedo.fx.Launcher} serves as the entry point for the
 * JavaFX application and integrates with Spring Boot for dependency injection and
 * application configuration.</p>
 * 
 * @since 1.0
 */
package io.joshuasalcedo.fx;