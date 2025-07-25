package io.joshuasalcedo.fx.domain.spec.java;

import io.joshuasalcedo.fx.domain.model.java.*;
import io.joshuasalcedo.fx.domain.spec.Specification;

/**
 * Specifications for code analysis business rules.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class JavaFileAnalysisSpecifications {

    private JavaFileAnalysisSpecifications() {}

    /**
     * Specification for files with high cyclomatic complexity.
     */
    public static Specification<JavaFile> hasHighCyclomaticComplexity() {
        return javaFile -> {
            // This would typically use the analyzer, but for specification we define the threshold
            return javaFile.getTypeDeclarations().stream()
                    .anyMatch(type -> type.methods().size() > 10 || 
                             type.methods().stream().anyMatch(method -> 
                                     method.body() != null && method.body().split("if|for|while|switch|catch").length > 10));
        };
    }

    /**
     * Specification for files with low maintainability.
     */
    public static Specification<JavaFile> hasLowMaintainability() {
        return javaFile -> {
            LinesOfCodeResult loc = calculateBasicLOC(javaFile);
            return loc.totalLines() > 500 || loc.commentRatio() < 0.1;
        };
    }

    /**
     * Specification for files that need urgent refactoring.
     */
    public static Specification<JavaFile> needsUrgentRefactoring() {
        return hasHighCyclomaticComplexity().and(hasLowMaintainability());
    }

    /**
     * Specification for well-structured files.
     */
    public static Specification<JavaFile> isWellStructured() {
        return javaFile -> {
            return javaFile.getTypeDeclarations().size() <= 3 && // Max 3 types per file
                   javaFile.getTypeDeclarations().stream()
                           .allMatch(type -> type.methods().size() <= 20) && // Max 20 methods per type
                   javaFile.getImports().size() <= 15; // Max 15 imports
        };
    }

    /**
     * Specification for files with good documentation.
     */
    public static Specification<JavaFile> hasGoodDocumentation() {
        return javaFile -> {
            LinesOfCodeResult loc = calculateBasicLOC(javaFile);
            return loc.commentRatio() >= 0.15; // At least 15% comments
        };
    }

    /**
     * Specification for files with too many dependencies.
     */
    public static Specification<JavaFile> hasTooManyDependencies() {
        return javaFile -> javaFile.getImports().size() > 20;
    }

    /**
     * Specification for files that are too large.
     */
    public static Specification<JavaFile> isTooLarge() {
        return javaFile -> {
            LinesOfCodeResult loc = calculateBasicLOC(javaFile);
            return loc.totalLines() > 1000;
        };
    }

    /**
     * Specification for files with complex method signatures.
     */
    public static Specification<JavaFile> hasComplexMethodSignatures() {
        return javaFile -> javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> type.methods().stream()
                        .anyMatch(method -> method.parameters().size() > 7)); // More than 7 parameters
    }

    /**
     * Specification for files that follow naming conventions.
     */
    public static Specification<JavaFile> followsNamingConventions() {
        return javaFile -> {
            // Check if type names are in PascalCase
            boolean typeNamesValid = javaFile.getTypeDeclarations().stream()
                    .allMatch(type -> Character.isUpperCase(type.name().charAt(0)));

            // Check if method names are in camelCase
            boolean methodNamesValid = javaFile.getTypeDeclarations().stream()
                    .flatMap(type -> type.methods().stream())
                    .allMatch(method -> Character.isLowerCase(method.name().charAt(0)));

            return typeNamesValid && methodNamesValid;
        };
    }

    /**
     * Specification for test files.
     */
    public static Specification<JavaFile> isTestFile() {
        return javaFile -> {
            String fileName = javaFile.getFileName();
            return fileName.endsWith("Test.java") || 
                   fileName.endsWith("Tests.java") ||
                   javaFile.getPackageName().contains("test") ||
                   javaFile.getImports().stream().anyMatch(imp -> 
                           imp.contains("junit") || imp.contains("testng") || imp.contains("mockito"));
        };
    }

    /**
     * Specification for files with main method (executable).
     */
    public static Specification<JavaFile> isExecutable() {
        return javaFile -> javaFile.getTypeDeclarations().stream()
                .anyMatch(JavaFile.TypeDeclaration::hasMainMethod);
    }

    /**
     * Specification for utility classes.
     */
    public static Specification<JavaFile> isUtilityClass() {
        return javaFile -> javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> 
                    type.modifiers().contains(JavaFile.Modifier.FINAL) &&
                    type.methods().stream().allMatch(method -> 
                            method.modifiers().contains(JavaFile.Modifier.STATIC)) &&
                    type.constructors().isEmpty());
    }

    /**
     * Composite specification for high-quality code.
     */
    public static Specification<JavaFile> isHighQuality() {
        return isWellStructured()
                .and(hasGoodDocumentation())
                .and(followsNamingConventions())
                .and(hasHighCyclomaticComplexity().not())
                .and(isTooLarge().not());
    }

    /**
     * Helper method to calculate basic lines of code metrics.
     */
    private static LinesOfCodeResult calculateBasicLOC(JavaFile javaFile) {
        if (javaFile.getFileContent() == null) {
            return new LinesOfCodeResult(0, 0, 0, 0, 0.0);
        }

        String[] lines = javaFile.getFileContent().split("\n");
        int totalLines = lines.length;
        int commentLines = 0;
        int blankLines = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                blankLines++;
            } else if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                commentLines++;
            }
        }

        int codeLines = totalLines - commentLines - blankLines;
        double commentRatio = totalLines > 0 ? (double) commentLines / totalLines : 0.0;

        return new LinesOfCodeResult(totalLines, codeLines, commentLines, blankLines, commentRatio);
    }
}