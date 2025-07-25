package io.joshuasalcedo.fx.domain.model.java;

import io.joshuasalcedo.fx.domain.annotation.AggregateRoot;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;


@AggregateRoot
public class JavaFile {
    private String fileName;
    private String fileContent;
    private String filePath;
    private final String fileExtension = ".java";
    private String packageName;
    private List<String> imports;
    private List<TypeDeclaration> typeDeclarations; // Can have multiple types in one file

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public List<TypeDeclaration> getTypeDeclarations() {
        return typeDeclarations;
    }

    public void setTypeDeclarations(List<TypeDeclaration> typeDeclarations) {
        this.typeDeclarations = typeDeclarations;
    }

    public Path getPath() {
        return Path.of(filePath);
    }

    public record TypeDeclaration(
            String name,
            TypeKind kind, // CLASS, INTERFACE, ENUM, RECORD
            Set<Modifier> modifiers,
            List<String> annotations,
            String superClass,
            List<String> interfaces,
            List<Field> fields,
            List<Constructor> constructors,
            List<Method> methods,
            List<TypeDeclaration> nestedTypes
    ) {

        public boolean hasMainMethod() {
            if (methods == null) {
                return false;
            }

            return methods.stream()
                    .anyMatch(method ->
                            "main".equals(method.name()) &&
                                    "void".equals(method.returnType()) &&
                                    method.modifiers().contains(Modifier.PUBLIC) &&
                                    method.modifiers().contains(Modifier.STATIC) &&
                                    hasValidMainParameters(method.parameters())
                    );
        }

        private boolean hasValidMainParameters(List<Parameter> parameters) {
            if (parameters == null || parameters.size() != 1) {
                return false;
            }

            Parameter param = parameters.getFirst();
            // Check for String[] or String... (varargs)
            return "String[]".equals(param.type()) ||
                    ("String".equals(param.type()) && param.isVarArgs());
        }
    }



    public record Method(
            String name,
            String returnType,
            List<Parameter> parameters,
            Set<Modifier> modifiers,
            List<String> annotations,
            String body
    ) {}

    public record Constructor(
            List<Parameter> parameters,
            Set<Modifier> modifiers,
            List<String> annotations,
            String body
    ) {}

    public record Field(
            String name,
            String type,
            Set<Modifier> modifiers,
            List<String> annotations,
            String initializer
    ) {}

    public record Parameter(
            String name,
            String type,
            List<String> annotations,
            boolean isFinal,
            boolean isVarArgs
    ) {}

    public enum TypeKind {
        CLASS, INTERFACE, ENUM, RECORD
    }

    public enum Modifier {
        PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL, ABSTRACT,
        SYNCHRONIZED, VOLATILE, TRANSIENT, NATIVE, STRICTFP
    }
}