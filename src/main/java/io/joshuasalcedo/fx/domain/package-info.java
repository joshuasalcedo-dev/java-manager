/**
 * This package contains the domain model classes that represent the core business concepts and logic.
 * 
 * <p>The domain layer is the heart of the application and encapsulates the business rules,
 * entities, value objects, and domain services. It represents the state and behavior
 * of the business domain independent of any technical implementation details.</p>
 * 
 * <p>Key components in this package include:
 * <ul>
 *   <li>Entity classes representing business objects with identity</li>
 *   <li>Value objects representing immutable concepts</li>
 *   <li>Domain events representing significant occurrences in the domain</li>
 *   <li>Domain services containing business logic that doesn't naturally fit in entities</li>
 *   <li>Repository interfaces defining how domain objects are persisted</li>
 * </ul>
 * </p>
 * 
 * <p>The domain model should be kept clean of infrastructure concerns and should focus
 * solely on expressing the business domain and its rules.</p>
 * 
 * @since 1.0
 */
package io.joshuasalcedo.fx.domain;
