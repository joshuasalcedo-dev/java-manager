package io.joshuasalcedo.fx.domain.service.java;

import io.joshuasalcedo.fx.domain.model.java.JavaFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * CompilerService interface for compiling Java source code.
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025
 * @since ${PROJECT.version}
 */
public interface CompilerService {
    
    /**
     * Compiles a single Java file.
     * 
     * @param file the Java source file to compile
     * @return the compilation result
     */
    CompilationResult compile(File file);
    
    /**
     * Compiles Java source code from a string.
     * 
     * @param sourceCode the Java source code
     * @param className the name of the class being compiled
     * @return the compilation result
     */
    CompilationResult compile(String sourceCode, String className);
    
    /**
     * Compiles multiple Java files.
     * 
     * @param files the Java source files to compile
     * @return the compilation result
     */
    CompilationResult compile(List<File> files);
    
    /**
     * Compiles all Java files in a directory.
     * 
     * @param directory the directory containing Java files
     * @param recursive whether to search subdirectories
     * @return the compilation result
     */
    CompilationResult compileDirectory(Path directory, boolean recursive);
    
    /**
     * Sets the classpath for compilation.
     * 
     * @param classpath list of classpath entries
     */
    void setClasspath(List<String> classpath);
    
    /**
     * Sets the output directory for compiled classes.
     * 
     * @param outputDirectory the output directory path
     */
    void setOutputDirectory(Path outputDirectory);
    
    /**
     * Sets additional compiler options.
     * 
     * @param options compiler options (e.g., "-g", "-verbose")
     */
    void setCompilerOptions(List<String> options);

    void runJar(File file);
    void runMain(JavaFile file);
    /**
     * Result of a compilation operation.
     */
    record CompilationResult(
        boolean success,
        List<Diagnostic> diagnostics,
        Optional<Path> outputPath,
        long compilationTimeMillis
    ) {
        public boolean hasErrors() {
            return diagnostics.stream().anyMatch(d -> d.severity() == Severity.ERROR);
        }
        
        public boolean hasWarnings() {
            return diagnostics.stream().anyMatch(d -> d.severity() == Severity.WARNING);
        }
    }
    
    /**
     * Compilation diagnostic message.
     */
    record Diagnostic(
        Severity severity,
        String message,
        Optional<String> source,
        Optional<Long> line,
        Optional<Long> column
    ) {}
    
    /**
     * Severity levels for diagnostics.
     */
    enum Severity {
        ERROR,
        WARNING,
        INFO,
        NOTE
    }
}