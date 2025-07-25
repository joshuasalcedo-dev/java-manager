package io.joshuasalcedo.fx.domain.service.java;

import io.joshuasalcedo.fx.domain.model.java.JavaFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Domain service interface for Java source code refactoring operations.
 * Implementations should be provided outside the domain layer.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public interface RefactorService {

    /**
     * Refactors package names across multiple Java files.
     * Updates package declarations and import statements.
     *
     * @param oldPackageName the current package name to refactor
     * @param newPackageName the new package name
     * @return list of files that were successfully refactored
     * @throws IllegalArgumentException if package names are null or invalid
     */
    RefactorResult refactorPackageName(String oldPackageName, String newPackageName);

    /**
     * Refactors a type name (class, interface, enum, record) across the codebase.
     * Updates type declarations, imports, and usages.
     *
     * @param oldTypeName the current type name
     * @param newTypeName the new type name
     * @param packageName the package where the type is located
     * @return refactor result with modified files and statistics
     */
    RefactorResult refactorTypeName(String oldTypeName, String newTypeName, String packageName);

    /**
     * Refactors method names across files.
     * Updates method declarations and method calls.
     *
     * @param methodName the current method name
     * @param newMethodName the new method name
     * @param className the class containing the method (null for all classes)
     * @return refactor result with modified files
     */
    RefactorResult refactorMethodName(String methodName, String newMethodName, String className);

    /**
     * Refactors field names across files.
     * Updates field declarations and field access.
     *
     * @param fieldName the current field name
     * @param newFieldName the new field name
     * @param className the class containing the field (null for all classes)
     * @return refactor result with modified files
     */
    RefactorResult refactorFieldName(String fieldName, String newFieldName, String className);

    /**
     * Moves a type from one package to another.
     * Combines type extraction and package refactoring.
     *
     * @param typeName the type to move
     * @param fromPackage source package
     * @param toPackage destination package
     * @return refactor result with moved files and updated imports
     */
    RefactorResult moveTypeToPackage(String typeName, String fromPackage, String toPackage);

    /**
     * Extracts a method to a new utility class.
     *
     * @param sourceClass the class containing the method
     * @param methodName the method to extract
     * @param targetClass the new utility class name
     * @param targetPackage the package for the utility class
     * @return refactor result with new utility class and updated references
     */
    RefactorResult extractMethodToUtilityClass(String sourceClass, String methodName, 
                                             String targetClass, String targetPackage);

    /**
     * Renames multiple entities in a single operation using a mapping.
     * More efficient than individual refactoring operations.
     *
     * @param renameMapping map of old names to new names
     * @param refactorType the type of entities to rename (PACKAGE, TYPE, METHOD, FIELD)
     * @return refactor result with all modifications
     */
    RefactorResult batchRefactor(Map<String, String> renameMapping, RefactorType refactorType);

    /**
     * Validates if a refactoring operation is safe to perform.
     * Checks for naming conflicts, circular dependencies, etc.
     *
     * @param operation the refactoring operation to validate
     * @return validation result with any issues found
     */
    RefactorValidationResult validateRefactoring(RefactorOperation operation);

    /**
     * Previews the changes that would result from a refactoring operation
     * without actually performing the changes.
     *
     * @param operation the refactoring operation to preview
     * @return preview result showing what would be changed
     */
    RefactorPreviewResult previewRefactoring(RefactorOperation operation);

    /**
     * Types of refactoring operations supported.
     */
    public enum RefactorType {
        PACKAGE,
        TYPE,
        METHOD, 
        FIELD,
        VARIABLE
    }

    /**
     * Result of a refactoring operation.
     */
   public class RefactorResult {
        private final List<JavaFile> modifiedFiles;
        private final List<JavaFile> createdFiles;
        private final List<String> deletedFiles;
        private final RefactorStatistics statistics;
        private final List<RefactorIssue> issues;
        private final boolean successful;

        public RefactorResult(List<JavaFile> modifiedFiles, List<JavaFile> createdFiles, 
                            List<String> deletedFiles, RefactorStatistics statistics,
                            List<RefactorIssue> issues, boolean successful) {
            this.modifiedFiles = List.copyOf(modifiedFiles);
            this.createdFiles = List.copyOf(createdFiles);
            this.deletedFiles = List.copyOf(deletedFiles);
            this.statistics = statistics;
            this.issues = List.copyOf(issues);
            this.successful = successful;
        }

        public List<JavaFile> getModifiedFiles() { return modifiedFiles; }
        public List<JavaFile> getCreatedFiles() { return createdFiles; }
        public List<String> getDeletedFiles() { return deletedFiles; }
        public RefactorStatistics getStatistics() { return statistics; }
        public List<RefactorIssue> getIssues() { return issues; }
        public boolean isSuccessful() { return successful; }
        public boolean hasIssues() { return !issues.isEmpty(); }
        
        public int getTotalFilesAffected() {
            return modifiedFiles.size() + createdFiles.size() + deletedFiles.size();
        }
    }

    /**
     * Statistics about a refactoring operation.
     */
    record RefactorStatistics(
        int filesAnalyzed,
        int filesModified,
        int filesCreated,
        int filesDeleted,
        int importsUpdated,
        int declarationsUpdated,
        int referencesUpdated,
        long executionTimeMs
    ) {
        public String toSummary() {
            return String.format(
                "Refactoring Stats: %d files analyzed, %d modified, %d created, %d deleted, " +
                "%d imports updated, %d declarations updated, %d references updated (took %dms)",
                filesAnalyzed, filesModified, filesCreated, filesDeleted,
                importsUpdated, declarationsUpdated, referencesUpdated, executionTimeMs
            );
        }
    }

    /**
     * An issue encountered during refactoring.
     */
    record RefactorIssue(
        Severity severity,
        String message,
        String fileName,
        Integer lineNumber,
        RefactorType affectedType
    ) {
        public enum Severity {
            ERROR,   // Prevents refactoring from completing
            WARNING, // Refactoring can proceed but may have issues
            INFO     // Informational message
        }
        
        public static RefactorIssue error(String message, String fileName) {
            return new RefactorIssue(Severity.ERROR, message, fileName, null, null);
        }
        
        public static RefactorIssue warning(String message, String fileName) {
            return new RefactorIssue(Severity.WARNING, message, fileName, null, null);
        }
        
        public static RefactorIssue info(String message) {
            return new RefactorIssue(Severity.INFO, message, null, null, null);
        }
    }

    /**
     * Validation result for refactoring operations.
     */
   public class RefactorValidationResult {
        private final boolean valid;
        private final List<RefactorIssue> issues;
        private final Set<String> conflictingNames;
        private final List<String> impactedFiles;

        public RefactorValidationResult(boolean valid, List<RefactorIssue> issues, 
                                      Set<String> conflictingNames, List<String> impactedFiles) {
            this.valid = valid;
            this.issues = List.copyOf(issues);
            this.conflictingNames = Set.copyOf(conflictingNames);
            this.impactedFiles = List.copyOf(impactedFiles);
        }

        public boolean isValid() { return valid; }
        public List<RefactorIssue> getIssues() { return issues; }
        public Set<String> getConflictingNames() { return conflictingNames; }
        public List<String> getImpactedFiles() { return impactedFiles; }
        public boolean hasErrors() { 
            return issues.stream().anyMatch(i -> i.severity() == RefactorIssue.Severity.ERROR);
        }
    }

    /**
     * Preview result showing what would change.
     */
    class RefactorPreviewResult {
        private final Map<String, String> fileChanges; // filename -> preview of changes
        private final List<String> filesToCreate;
        private final List<String> filesToDelete;
        private final RefactorStatistics estimatedStatistics;

        public RefactorPreviewResult(Map<String, String> fileChanges, List<String> filesToCreate,
                                   List<String> filesToDelete, RefactorStatistics estimatedStatistics) {
            this.fileChanges = Map.copyOf(fileChanges);
            this.filesToCreate = List.copyOf(filesToCreate);
            this.filesToDelete = List.copyOf(filesToDelete);
            this.estimatedStatistics = estimatedStatistics;
        }

        public Map<String, String> getFileChanges() { return fileChanges; }
        public List<String> getFilesToCreate() { return filesToCreate; }
        public List<String> getFilesToDelete() { return filesToDelete; }
        public RefactorStatistics getEstimatedStatistics() { return estimatedStatistics; }
    }

    /**
     * Represents a refactoring operation to be performed.
     */
    abstract class RefactorOperation {
        private final RefactorType type;
        private final String description;

        protected RefactorOperation(RefactorType type, String description) {
            this.type = type;
            this.description = description;
        }

        public RefactorType getType() { return type; }
        public String getDescription() { return description; }

        /**
         * Package refactoring operation.
         */
        public static class PackageRefactor extends RefactorOperation {
            private final String oldPackageName;
            private final String newPackageName;

            public PackageRefactor(String oldPackageName, String newPackageName) {
                super(RefactorType.PACKAGE, "Rename package " + oldPackageName + " to " + newPackageName);
                this.oldPackageName = oldPackageName;
                this.newPackageName = newPackageName;
            }

            public String getOldPackageName() { return oldPackageName; }
            public String getNewPackageName() { return newPackageName; }
        }

        /**
         * Type refactoring operation.
         */
        public static class TypeRefactor extends RefactorOperation {
            private final String oldTypeName;
            private final String newTypeName;
            private final String packageName;

            public TypeRefactor(String oldTypeName, String newTypeName, String packageName) {
                super(RefactorType.TYPE, "Rename type " + oldTypeName + " to " + newTypeName);
                this.oldTypeName = oldTypeName;
                this.newTypeName = newTypeName;
                this.packageName = packageName;
            }

            public String getOldTypeName() { return oldTypeName; }
            public String getNewTypeName() { return newTypeName; }
            public String getPackageName() { return packageName; }
        }

        /**
         * Method refactoring operation.
         */
        public static class MethodRefactor extends RefactorOperation {
            private final String oldMethodName;
            private final String newMethodName;
            private final String className;

            public MethodRefactor(String oldMethodName, String newMethodName, String className) {
                super(RefactorType.METHOD, "Rename method " + oldMethodName + " to " + newMethodName + 
                      (className != null ? " in " + className : ""));
                this.oldMethodName = oldMethodName;
                this.newMethodName = newMethodName;
                this.className = className;
            }

            public String getOldMethodName() { return oldMethodName; }
            public String getNewMethodName() { return newMethodName; }
            public String getClassName() { return className; }
        }
    }
}