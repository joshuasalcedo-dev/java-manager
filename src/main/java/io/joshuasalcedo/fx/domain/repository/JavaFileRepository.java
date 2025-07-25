package io.joshuasalcedo.fx.domain.repository;

import io.joshuasalcedo.fx.domain.model.java.JavaFile;
import io.joshuasalcedo.fx.domain.model.java.JavaFile.TypeKind;
import io.joshuasalcedo.fx.domain.spec.Specification;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Java source files with advanced search capabilities.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public interface JavaFileRepository {

    /**
     * Find a Java file by its path
     */
    Optional<JavaFile> findByPath(Path filePath);

    /**
     * Find Java files by package name
     */
    List<JavaFile> findByPackageName(String packageName);

    /**
     * Find Java files by class/type name
     */
    List<JavaFile> findByTypeName(String typeName);

    /**
     * Find Java files containing specific text in content (fast full-text search)
     */
    List<JavaFile> searchContent(String searchText);

    /**
     * Find Java files containing specific text with advanced options
     */
    List<JavaFile> searchContent(ContentSearchQuery query);

    /**
     * Find Java files that import a specific class or package
     */
    List<JavaFile> findByImport(String importPattern);

    /**
     * Find Java files by type kind (CLASS, INTERFACE, ENUM, RECORD)
     */
    List<JavaFile> findByTypeKind(TypeKind typeKind);

    /**
     * Find Java files that extend or implement specific type
     */
    List<JavaFile> findByParentType(String parentTypeName);

    /**
     * Find Java files with specific annotations
     */
    List<JavaFile> findByAnnotation(String annotationName);

    /**
     * Find Java files containing methods with specific name
     */
    List<JavaFile> findByMethodName(String methodName);

    /**
     * Find Java files that satisfy a specification
     */
    List<JavaFile> findBySpecification(Specification<JavaFile> specification);

    /**
     * Find all Java files in a directory (recursive)
     */
    List<JavaFile> findAllInDirectory(Path directory);

    /**
     * Find all test files
     */
    List<JavaFile> findTestFiles();

    /**
     * Find all executable files (with main method)
     */
    List<JavaFile> findExecutableFiles();

    /**
     * Find files that reference a specific type
     */
    List<JavaFile> findReferencingType(String typeName);

    /**
     * Find files in specific packages (with wildcards)
     */
    List<JavaFile> findByPackagePattern(String packagePattern);

    /**
     * Save a Java file
     */
    JavaFile save(JavaFile javaFile);

    List<JavaFile> getAll();

    /**
     * Save multiple Java files
     */
    List<JavaFile> saveAll(List<JavaFile> javaFiles);

    /**
     * Delete a Java file
     */
    void delete(JavaFile javaFile);

    /**
     * Delete by path
     */
    void deleteByPath(Path filePath);

    /**
     * Check if a file exists at path
     */
    boolean existsAtPath(Path filePath);


    /**
     * Count all Java files
     */
    long count();

    /**
     * Count files that satisfy a specification
     */
    long count(Specification<JavaFile> specification);

    /**
     * Get all unique package names
     */
    Set<String> findAllPackageNames();

    /**
     * Get all unique type names
     */
    Set<String> findAllTypeNames();

    /**
     * Rebuild search index for fast content searching
     */
    void rebuildSearchIndex();

    /**
     * Advanced content search query
     */
    class ContentSearchQuery {
        private final String searchText;
        private final boolean caseSensitive;
        private final boolean wholeWord;
        private final boolean useRegex;
        private final Set<String> includePackages;
        private final Set<String> excludePackages;
        private final Integer maxResults;

        private ContentSearchQuery(Builder builder) {
            this.searchText = builder.searchText;
            this.caseSensitive = builder.caseSensitive;
            this.wholeWord = builder.wholeWord;
            this.useRegex = builder.useRegex;
            this.includePackages = builder.includePackages;
            this.excludePackages = builder.excludePackages;
            this.maxResults = builder.maxResults;
        }

        public static Builder builder(String searchText) {
            return new Builder(searchText);
        }

        // Getters
        public String getSearchText() { return searchText; }
        public boolean isCaseSensitive() { return caseSensitive; }
        public boolean isWholeWord() { return wholeWord; }
        public boolean isUseRegex() { return useRegex; }
        public Set<String> getIncludePackages() { return includePackages; }
        public Set<String> getExcludePackages() { return excludePackages; }
        public Integer getMaxResults() { return maxResults; }

        public static class Builder {
            private final String searchText;
            private boolean caseSensitive = false;
            private boolean wholeWord = false;
            private boolean useRegex = false;
            private Set<String> includePackages = Set.of();
            private Set<String> excludePackages = Set.of();
            private Integer maxResults = null;

            private Builder(String searchText) {
                this.searchText = searchText;
            }

            public Builder caseSensitive(boolean caseSensitive) {
                this.caseSensitive = caseSensitive;
                return this;
            }

            public Builder wholeWord(boolean wholeWord) {
                this.wholeWord = wholeWord;
                return this;
            }

            public Builder useRegex(boolean useRegex) {
                this.useRegex = useRegex;
                return this;
            }

            public Builder includePackages(Set<String> packages) {
                this.includePackages = packages;
                return this;
            }

            public Builder excludePackages(Set<String> packages) {
                this.excludePackages = packages;
                return this;
            }

            public Builder maxResults(int maxResults) {
                this.maxResults = maxResults;
                return this;
            }

            public ContentSearchQuery build() {
                return new ContentSearchQuery(this);
            }
        }
    }
}