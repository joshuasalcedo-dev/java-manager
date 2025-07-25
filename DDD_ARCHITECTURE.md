# Domain Driven Design Architecture

This project now follows Domain Driven Design (DDD) principles with a well-structured domain layer containing business rules, specifications, services, and repositories.

## Domain Structure

```
src/main/java/io/joshuasalcedo/fx/domain/
├── event/                          # Domain Events
│   ├── DomainEvent.java            # Base event interface
│   └── maven/
│       ├── ProjectCreatedEvent.java
│       └── DependencyAddedEvent.java
├── maven/                          # Aggregate Roots & Entities  
│   ├── Project.java               # Rich domain entity with business methods
│   ├── Dependency.java            # Rich domain entity with business methods
│   ├── Coordinates.java           # Value object
│   ├── Parent.java                # Value object
│   └── ProjectMetadata.java       # Entity
├── repository/                     # Repository Interfaces (Domain Layer)
│   ├── ProjectRepository.java      # Project repository contract
│   └── DependencyRepository.java   # Dependency repository contract
├── service/                        # Domain Services
│   ├── ProjectDomainService.java   # Complex project business logic
│   └── DependencyDomainService.java # Complex dependency business logic
├── spec/                          # Business Rules as Specifications
│   ├── Specification.java          # Base specification interface
│   └── maven/
│       ├── ProjectSpecifications.java      # Project business rules
│       ├── DependencySpecifications.java   # Dependency business rules
│       └── CoordinatesSpecifications.java  # Coordinates business rules
└── value/                         # Value Objects
    ├── Version.java               # Rich version value object
    ├── GroupId.java               # Maven groupId
    ├── ArtifactId.java           # Maven artifactId
    └── Scope.java                # Maven dependency scope
```

## Key DDD Patterns Implemented

### 1. Aggregate Roots
- **Project**: Main aggregate root managing modules, dependencies, and project lifecycle
- **Dependency**: Entity within Project aggregate, can also be used independently

### 2. Value Objects
- **Coordinates**: Immutable GAV coordinates
- **Version**: Rich version handling with semantic versioning, snapshots, properties
- **GroupId, ArtifactId, Scope**: Type-safe Maven identifiers

### 3. Specifications Pattern
Business rules are encapsulated in specifications that can be composed:

```java
// Example usage
var validMavenProject = ProjectSpecifications.isValidMavenProject();
var multiModuleProjects = ProjectSpecifications.isMultiModuleProject();
var complexRule = validMavenProject.and(multiModuleProjects);

boolean isValid = complexRule.isSatisfiedBy(project);
```

### 4. Repository Pattern
Domain-focused repository interfaces define what the domain needs:

```java
public interface ProjectRepository {
    Optional<Project> findByCoordinates(Coordinates coordinates);
    List<Project> findBySpecification(Specification<Project> specification);
    // ... other domain-focused methods
}
```

### 5. Domain Services
Complex business logic that doesn't naturally fit in entities:

```java
public class ProjectDomainService {
    public ProjectValidationResult validateProject(Project project);
    public List<Dependency> resolveDependencyVersions(Project project, List<Dependency> dependencies);
    // ... other complex business operations
}
```

### 6. Rich Domain Objects
Entities contain business methods, not just data:

```java
public class Project {
    // Business methods
    public boolean isMultiModuleProject() { /* ... */ }
    public void addDependency(Dependency dependency) { /* business logic */ }
    public List<Dependency> getTestDependencies() { /* ... */ }
    public Coordinates getEffectiveCoordinates() { /* inheritance logic */ }
    // ... many more business methods
}
```

### 7. Domain Events
Key business events that other bounded contexts might care about:

```java
public record ProjectCreatedEvent(
    UUID eventId,
    Instant occurredOn,
    Coordinates projectCoordinates,
    String rootPath,
    boolean hasParent
) implements DomainEvent
```

## Business Rules Captured in Specifications

### Project Rules
- `hasCoordinates()`: Project must have GAV coordinates
- `hasValidRootPath()`: Project must have existing root directory
- `hasPomFile()`: Project must contain pom.xml
- `standaloneProjectHasCompleteCoordinates()`: Non-child projects need full GAV
- `childProjectCanInheritFromParent()`: Child projects can inherit groupId/version
- `isValidMavenProject()`: Complete Maven project validation

### Dependency Rules  
- `hasRequiredCoordinates()`: Must have groupId and artifactId
- `hasExplicitVersion()`: Has concrete version (not property/inherited)
- `usesPropertyVersion()`: Uses ${property} version format
- `isSnapshotDependency()`: Version ends with -SNAPSHOT
- `needsVersionResolution()`: Missing version or uses property
- `canBeConvertedToVersionProperty()`: Can be converted to use properties

### Coordinates Rules
- `isComplete()`: Has groupId, artifactId, and version (GAV)
- `isPartial()`: Has groupId and artifactId only (GA)
- `isSnapshot()`: Version is snapshot
- `usesPropertyVersion()`: Version is a property placeholder

## Benefits of This DDD Structure

1. **Business Logic Centralization**: All Maven-specific business rules are in the domain layer
2. **Testability**: Specifications and domain services are easily unit testable
3. **Flexibility**: Business rules can be composed and reused
4. **Maintainability**: Changes to business rules are isolated to specifications
5. **Rich Domain Model**: Entities contain behavior, not just data
6. **Clear Dependencies**: Domain layer has no external dependencies
7. **Event-Driven**: Domain events enable loose coupling between bounded contexts

## Next Steps for Full DDD Implementation

1. **Infrastructure Layer**: Implement repository interfaces
2. **Application Services**: Coordinate domain services and repositories  
3. **Event Publishing**: Implement domain event publishing mechanism
4. **Bounded Context Integration**: Define how this domain interacts with others
5. **Domain Event Handlers**: React to domain events from other contexts

The domain layer is now focused purely on Maven project management business logic, with clear separation of concerns and rich business behavior encapsulated in the appropriate domain objects.