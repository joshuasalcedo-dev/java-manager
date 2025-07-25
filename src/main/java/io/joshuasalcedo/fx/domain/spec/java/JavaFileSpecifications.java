package io.joshuasalcedo.fx.domain.spec.java;

import io.joshuasalcedo.fx.domain.model.java.JavaFile;
import io.joshuasalcedo.fx.domain.model.java.JavaFile.TypeDeclaration;
import io.joshuasalcedo.fx.domain.model.java.JavaFile.TypeKind;
import io.joshuasalcedo.fx.domain.model.java.JavaFile.Modifier;
import io.joshuasalcedo.fx.domain.spec.Specification;

/**
 * Specifications for Java source file business rules.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public final class JavaFileSpecifications {

    private JavaFileSpecifications() {
        // Utility class
    }

    /**
     * Java file must have a valid file name
     */
    public static Specification<JavaFile> hasValidFileName() {
        return javaFile -> 
            javaFile.getFileName() != null && 
            !javaFile.getFileName().isEmpty() &&
            javaFile.getFileName().endsWith(".java");
    }

    /**
     * Java file must have a package declaration
     */
    public static Specification<JavaFile> hasPackageDeclaration() {
        return javaFile -> 
            javaFile.getPackageName() != null && 
            !javaFile.getPackageName().isEmpty();
    }

    /**
     * Java file follows default package (no package declaration)
     */
    public static Specification<JavaFile> isInDefaultPackage() {
        return javaFile -> 
            javaFile.getPackageName() == null || 
            javaFile.getPackageName().isEmpty();
    }

    /**
     * Java file has at least one type declaration
     */
    public static Specification<JavaFile> hasTypeDeclarations() {
        return javaFile -> 
            javaFile.getTypeDeclarations() != null && 
            !javaFile.getTypeDeclarations().isEmpty();
    }

    /**
     * Java file has exactly one public type
     */
    public static Specification<JavaFile> hasExactlyOnePublicType() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            long publicTypeCount = javaFile.getTypeDeclarations().stream()
                .filter(type -> type.modifiers() != null && type.modifiers().contains(Modifier.PUBLIC))
                .count();
                
            return publicTypeCount == 1;
        };
    }

    /**
     * Public type name matches file name
     */
    public static Specification<JavaFile> publicTypeMatchesFileName() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null || javaFile.getFileName() == null) {
                return false;
            }
            
            String expectedTypeName = javaFile.getFileName().replace(".java", "");
            
            return javaFile.getTypeDeclarations().stream()
                .filter(type -> type.modifiers() != null && type.modifiers().contains(Modifier.PUBLIC))
                .anyMatch(type -> expectedTypeName.equals(type.name()));
        };
    }

    /**
     * Java file is a valid public Java file (follows naming conventions)
     */
    public static Specification<JavaFile> isValidPublicJavaFile() {
        return hasValidFileName()
            .and(hasTypeDeclarations())
            .and(hasExactlyOnePublicType())
            .and(publicTypeMatchesFileName());
    }

    /**
     * Java file contains a main method (executable)
     */
    public static Specification<JavaFile> hasMainMethod() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(TypeDeclaration::hasMainMethod);
        };
    }

    /**
     * Java file is an executable application
     */
    public static Specification<JavaFile> isExecutableApplication() {
        return hasMainMethod()
            .and(hasPublicClass());
    }

    /**
     * Java file contains only interfaces
     */
    public static Specification<JavaFile> containsOnlyInterfaces() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null || javaFile.getTypeDeclarations().isEmpty()) {
                return false;
            }
            
            return javaFile.getTypeDeclarations().stream()
                .allMatch(type -> type.kind() == TypeKind.INTERFACE);
        };
    }

    /**
     * Java file contains at least one class
     */
    public static Specification<JavaFile> containsClass() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> type.kind() == TypeKind.CLASS);
        };
    }

    /**
     * Java file contains at least one interface
     */
    public static Specification<JavaFile> containsInterface() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> type.kind() == TypeKind.INTERFACE);
        };
    }

    /**
     * Java file contains at least one enum
     */
    public static Specification<JavaFile> containsEnum() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> type.kind() == TypeKind.ENUM);
        };
    }

    /**
     * Java file contains at least one record
     */
    public static Specification<JavaFile> containsRecord() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> type.kind() == TypeKind.RECORD);
        };
    }

    /**
     * Java file has at least one public class
     */
    public static Specification<JavaFile> hasPublicClass() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> 
                    type.kind() == TypeKind.CLASS && 
                    type.modifiers() != null && 
                    type.modifiers().contains(Modifier.PUBLIC));
        };
    }

    /**
     * Java file is a test file (by naming convention)
     */
    public static Specification<JavaFile> isTestFile() {
        return javaFile -> {
            if (javaFile.getFileName() == null) return false;
            
            String fileName = javaFile.getFileName();
            return fileName.endsWith("Test.java") || 
                   fileName.endsWith("Tests.java") ||
                   fileName.endsWith("TestCase.java") ||
                   fileName.contains("Test") && fileName.endsWith(".java");
        };
    }

    /**
     * Java file is in test package
     */
    public static Specification<JavaFile> isInTestPackage() {
        return javaFile -> {
            if (javaFile.getPackageName() == null) return false;
            
            return javaFile.getPackageName().contains(".test") ||
                   javaFile.getPackageName().contains(".tests") ||
                   javaFile.getPackageName().startsWith("test.") ||
                   javaFile.getPackageName().startsWith("tests.");
        };
    }

    /**
     * Java file is a test file (by convention or package)
     */
    public static Specification<JavaFile> isTestRelated() {
        return isTestFile().or(isInTestPackage());
    }

    /**
     * Java file has imports
     */
    public static Specification<JavaFile> hasImports() {
        return javaFile -> 
            javaFile.getImports() != null && 
            !javaFile.getImports().isEmpty();
    }

    /**
     * Java file uses specific import
     */
    public static Specification<JavaFile> usesImport(String importPattern) {
        return javaFile -> {
            if (javaFile.getImports() == null) return false;
            
            return javaFile.getImports().stream()
                .anyMatch(imp -> imp.contains(importPattern));
        };
    }

    /**
     * Java file is a JUnit test (imports JUnit)
     */
    public static Specification<JavaFile> isJUnitTest() {
        return usesImport("org.junit")
            .or(usesImport("junit.framework"));
    }

    /**
     * Java file is a TestNG test
     */
    public static Specification<JavaFile> isTestNGTest() {
        return usesImport("org.testng");
    }

    /**
     * Java file contains abstract types
     */
    public static Specification<JavaFile> containsAbstractTypes() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> 
                    type.modifiers() != null && 
                    type.modifiers().contains(Modifier.ABSTRACT));
        };
    }

    /**
     * Java file contains only abstract types
     */
    public static Specification<JavaFile> containsOnlyAbstractTypes() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null || javaFile.getTypeDeclarations().isEmpty()) {
                return false;
            }
            
            return javaFile.getTypeDeclarations().stream()
                .allMatch(type -> 
                    type.kind() == TypeKind.INTERFACE ||
                    (type.modifiers() != null && type.modifiers().contains(Modifier.ABSTRACT)));
        };
    }

    /**
     * Java file contains nested types
     */
    public static Specification<JavaFile> containsNestedTypes() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .anyMatch(type -> 
                    type.nestedTypes() != null && 
                    !type.nestedTypes().isEmpty());
        };
    }

    /**
     * Java file is a utility class (only static methods)
     */
    public static Specification<JavaFile> isUtilityClass() {
        return javaFile -> {
            if (javaFile.getTypeDeclarations() == null) return false;
            
            return javaFile.getTypeDeclarations().stream()
                .filter(type -> type.kind() == TypeKind.CLASS)
                .anyMatch(type -> {
                    // Check if all methods are static
                    if (type.methods() == null || type.methods().isEmpty()) {
                        return false;
                    }
                    
                    boolean allMethodsStatic = type.methods().stream()
                        .allMatch(method -> 
                            method.modifiers() != null && 
                            method.modifiers().contains(Modifier.STATIC));
                    
                    // Check if has private constructor (common pattern)
                    boolean hasPrivateConstructor = type.constructors() != null &&
                        type.constructors().stream()
                            .anyMatch(ctor -> 
                                ctor.modifiers() != null && 
                                ctor.modifiers().contains(Modifier.PRIVATE));
                    
                    return allMethodsStatic || hasPrivateConstructor;
                });
        };
    }

    /**
     * Java file follows standard package naming (lowercase)
     */
    public static Specification<JavaFile> hasStandardPackageNaming() {
        return javaFile -> {
            if (javaFile.getPackageName() == null || javaFile.getPackageName().isEmpty()) {
                return true; // Default package is valid
            }
            
            return javaFile.getPackageName().equals(javaFile.getPackageName().toLowerCase());
        };
    }

    /**
     * Java file has valid content (not empty)
     */
    public static Specification<JavaFile> hasContent() {
        return javaFile -> 
            javaFile.getFileContent() != null && 
            !javaFile.getFileContent().trim().isEmpty();
    }

    /**
     * Java file is a complete and valid Java source file
     */
    public static Specification<JavaFile> isCompleteJavaFile() {
        return hasValidFileName()
            .and(hasContent())
            .and(hasTypeDeclarations())
            .and(hasStandardPackageNaming());
    }
}