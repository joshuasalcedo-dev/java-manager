package io.joshuasalcedo.fx.domain.service.java;


import io.joshuasalcedo.fx.domain.annotation.DomainService;
import io.joshuasalcedo.fx.domain.model.java.JavaFile;
import io.joshuasalcedo.fx.domain.repository.JavaFileRepository;
import io.joshuasalcedo.fx.domain.spec.Specification;
import io.joshuasalcedo.fx.domain.spec.java.JavaFileAnalysisSpecifications;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * JavaFileService class.
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025 12:49 PM
 * @since ${PROJECT.version}
 */
@DomainService
public class JavaFileService {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(JavaFileService.class);

    private final JavaFileRepository javaFileRepository;
    private final RefactorService refactorService;
    private final CompilerService compilerService;
    private final JavaFileAnalyzerService javaFileAnalyzerService;
    public JavaFileService(JavaFileRepository javaFileRepository, RefactorService refactorService, CompilerService compilerService, JavaFileAnalyzerService javaFileAnalyzerService) {
        this.javaFileAnalyzerService = javaFileAnalyzerService;
        logger.info(MarkerFactory.getMarker("DomainService"), "JavaFileService created");
        this.compilerService = compilerService;

        this.javaFileRepository = javaFileRepository;
        this.refactorService = refactorService;
    }




    public JavaFile save(JavaFile javaFile) {
        logger.info(MarkerFactory.getMarker("save"), "Saving JavaFile {}", javaFile.getFileName());
        JavaFile result = javaFileRepository.save(javaFile);
        logger.info(MarkerFactory.getMarker("save"), "Successfully saved JavaFile {} with path: {}", 
                   result.getFileName(), result.getFilePath());
        return result;
    }

    public void delete(JavaFile javaFile) {
        logger.info(MarkerFactory.getMarker("delete"), "Deleting JavaFile {}", javaFile.getFileName());
        javaFileRepository.delete(javaFile);
        logger.info(MarkerFactory.getMarker("delete"), "Successfully deleted JavaFile {}", javaFile.getFileName());
    }

    public List<JavaFile> findAllInDirectory(Path directory) {
        logger.info("Finding all JavaFiles in directory: {}", directory);
        List<JavaFile> result = javaFileRepository.findAllInDirectory(directory);
        logger.info("Found {} JavaFiles in directory: {}", result.size(), directory);
        if (logger.isDebugEnabled()) {
            result.forEach(file -> logger.debug("Found file: {} at path: {}", file.getFileName(), file.getFilePath()));
        }
        return result;
    }

    public List<JavaFile> findAllWithSpec(Specification<JavaFile> specification) {
        logger.info("Finding JavaFiles with specification: {}", specification.getClass().getSimpleName());
        List<JavaFile> result = javaFileRepository.findBySpecification(specification);
        logger.info("Found {} JavaFiles matching specification", result.size());
        if (logger.isDebugEnabled()) {
            result.forEach(file -> logger.debug("Matching file: {} at path: {}", file.getFileName(), file.getFilePath()));
        }
        return result;
    }

    /**
     * Refactors package names using the injected RefactorService.
     * 
     * @param oldPackageName the current package name
     * @param newPackageName the new package name
     * @return list of modified files
     */
    public RefactorService.RefactorResult refactorPackageName(String oldPackageName, String newPackageName) {
        logger.info("Delegating package refactoring from '{}' to '{}' to RefactorService", 
                   oldPackageName, newPackageName);

        RefactorService.RefactorResult result = refactorService.refactorPackageName(oldPackageName, newPackageName);

        if (result.isSuccessful()) {
            logger.info("Package refactoring completed successfully: {} files modified", 
                       result.getTotalFilesAffected());
        } else {
            logger.warn("Package refactoring completed with issues: {} errors, {} warnings",
                       result.getIssues().stream().filter(i -> i.severity() == RefactorService.RefactorIssue.Severity.ERROR).count(),
                       result.getIssues().stream().filter(i -> i.severity() == RefactorService.RefactorIssue.Severity.WARNING).count());
        }

        return result;
    }

    /**
     * Convenience method that returns only the modified files for backward compatibility.
     */
    public List<JavaFile> refactorPackageNameSimple(String oldPackageName, String newPackageName) {
        RefactorService.RefactorResult result = refactorPackageName(oldPackageName, newPackageName);
        return result.getModifiedFiles();
    }

    /**
     * Refactors a type name using the RefactorService.
     */
    public RefactorService.RefactorResult refactorTypeName(String oldTypeName, String newTypeName, String packageName) {
        logger.info("Delegating type refactoring from '{}' to '{}' in package '{}'", 
                   oldTypeName, newTypeName, packageName);
        return refactorService.refactorTypeName(oldTypeName, newTypeName, packageName);
    }

    /**
     * Refactors a method name using the RefactorService.
     */
    public RefactorService.RefactorResult refactorMethodName(String methodName, String newMethodName, String className) {
        logger.info("Delegating method refactoring from '{}' to '{}' in class '{}'", 
                   methodName, newMethodName, className);
        return refactorService.refactorMethodName(methodName, newMethodName, className);
    }

    /**
     * Validates a refactoring operation before executing it.
     */
    public RefactorService.RefactorValidationResult validateRefactoring(RefactorService.RefactorOperation operation) {
        logger.debug("Validating refactoring operation: {}", operation.getDescription());
        return refactorService.validateRefactoring(operation);
    }

    /**
     * Previews the changes that would result from a refactoring operation.
     */
    public RefactorService.RefactorPreviewResult previewRefactoring(RefactorService.RefactorOperation operation) {
        logger.debug("Previewing refactoring operation: {}", operation.getDescription());
        return refactorService.previewRefactoring(operation);
    }

    /**
     * Moves a type from one package to another.
     */
    public RefactorService.RefactorResult moveTypeToPackage(String typeName, String fromPackage, String toPackage) {
        logger.info("Moving type '{}' from package '{}' to '{}'", typeName, fromPackage, toPackage);
        return refactorService.moveTypeToPackage(typeName, fromPackage, toPackage);
    }

    /**
     * Compiles a single Java file using the CompilerService.
     */
    public CompilerService.CompilationResult compile(JavaFile javaFile) {
        logger.info("Compiling Java file: {}", javaFile.getFileName());
        CompilerService.CompilationResult result = compilerService.compile(javaFile.getPath().toFile());

        if (result.success()) {
            logger.info("Compilation successful for {} in {}ms. Output: {}", 
                       javaFile.getFileName(), result.compilationTimeMillis(), 
                       result.outputPath().orElse(null));
        } else {
            long errorCount = result.diagnostics().stream().filter(d -> d.severity() == CompilerService.Severity.ERROR).count();
            long warningCount = result.diagnostics().stream().filter(d -> d.severity() == CompilerService.Severity.WARNING).count();
            logger.warn("Compilation failed for {} with {} errors, {} warnings in {}ms", 
                       javaFile.getFileName(), errorCount, warningCount, result.compilationTimeMillis());

            if (logger.isDebugEnabled()) {
                result.diagnostics().forEach(diagnostic -> 
                    logger.debug("Diagnostic [{}]: {} at line {}", 
                               diagnostic.severity(), diagnostic.message(), diagnostic.line().orElse(-1L)));
            }
        }

        return result;
    }

    /**
     * Compiles Java source code from string content.
     */
    public CompilerService.CompilationResult compileFromSource(String sourceCode, String className) {
        logger.info("Compiling source code for class: {} ({} characters)", className, sourceCode.length());
        CompilerService.CompilationResult result = compilerService.compile(sourceCode, className);

        if (result.success()) {
            logger.info("Source compilation successful for class {} in {}ms. Output: {}", 
                       className, result.compilationTimeMillis(), result.outputPath().orElse(null));
        } else {
            long errorCount = result.diagnostics().stream().filter(d -> d.severity() == CompilerService.Severity.ERROR).count();
            long warningCount = result.diagnostics().stream().filter(d -> d.severity() == CompilerService.Severity.WARNING).count();
            logger.warn("Source compilation failed for class {} with {} errors, {} warnings in {}ms", 
                       className, errorCount, warningCount, result.compilationTimeMillis());

            if (logger.isDebugEnabled()) {
                result.diagnostics().forEach(diagnostic -> 
                    logger.debug("Source diagnostic [{}]: {} at line {}", 
                               diagnostic.severity(), diagnostic.message(), diagnostic.line().orElse(-1L)));
            }
        }

        return result;
    }

    /**
     * Compiles multiple Java files using the CompilerService.
     */
    public CompilerService.CompilationResult compileAll(List<JavaFile> javaFiles) {
        logger.info("Compiling {} Java files in batch", javaFiles.size());
        if (logger.isDebugEnabled()) {
            javaFiles.forEach(file -> logger.debug("Batch compiling: {}", file.getFileName()));
        }

        List<java.io.File> files = javaFiles.stream()
                .map(jf -> Path.of(jf.getFilePath()).toFile())
                .toList();

        CompilerService.CompilationResult result = compilerService.compile(files);

        if (result.success()) {
            logger.info("Batch compilation completed successfully for {} files in {}ms. Output: {}", 
                       javaFiles.size(), result.compilationTimeMillis(), result.outputPath().orElse(null));
        } else {
            long errorCount = result.diagnostics().stream().filter(d -> d.severity() == CompilerService.Severity.ERROR).count();
            long warningCount = result.diagnostics().stream().filter(d -> d.severity() == CompilerService.Severity.WARNING).count();
            logger.warn("Batch compilation completed with {} errors, {} warnings in {}ms", 
                       errorCount, warningCount, result.compilationTimeMillis());

            if (logger.isDebugEnabled()) {
                result.diagnostics().forEach(diagnostic -> 
                    logger.debug("Batch diagnostic [{}]: {} in {} at line {}", 
                               diagnostic.severity(), diagnostic.message(), 
                               diagnostic.source().orElse("unknown"), diagnostic.line().orElse(-1L)));
            }
        }

        return result;
    }

    /**
     * Checks if a Java file compiles successfully.
     */
    public boolean canCompile(JavaFile javaFile) {
        logger.debug("Checking if file can compile: {}", javaFile.getFileName());
        CompilerService.CompilationResult result = compile(javaFile);
        boolean canCompile = result.success();
        logger.info("File {} compilation check result: {}", javaFile.getFileName(), 
                   canCompile ? "CAN COMPILE" : "CANNOT COMPILE");
        return canCompile;
    }

    /**
     * Gets compilation diagnostics for a Java file.
     */
    public List<CompilerService.Diagnostic> getDiagnostics(JavaFile javaFile) {
        logger.debug("Getting compilation diagnostics for: {}", javaFile.getFileName());
        CompilerService.CompilationResult result = compilerService.compile(javaFile.getPath().toFile());
        List<CompilerService.Diagnostic> diagnostics = result.diagnostics();
        logger.info("Retrieved {} diagnostics for {}: {} errors, {} warnings", 
                   diagnostics.size(), javaFile.getFileName(),
                   diagnostics.stream().filter(d -> d.severity() == CompilerService.Severity.ERROR).count(),
                   diagnostics.stream().filter(d -> d.severity() == CompilerService.Severity.WARNING).count());
        return diagnostics;
    }

    /**
     * Compiles files in a directory using the CompilerService.
     */
    public CompilerService.CompilationResult compileDirectory(Path directory, boolean recursive) {
        logger.info("Compiling all Java files in directory: {} (recursive: {})", directory, recursive);
        return compilerService.compileDirectory(directory, recursive);
    }

    /**
     * Sets the classpath for compilation.
     */
    public void setClasspath(List<String> classpath) {
        logger.info("Setting classpath with {} entries", classpath.size());
        compilerService.setClasspath(classpath);
    }

    /**
     * Sets the output directory for compiled classes.
     */
    public void setOutputDirectory(Path outputDirectory) {
        logger.info("Setting output directory to: {}", outputDirectory);
        compilerService.setOutputDirectory(outputDirectory);
    }

    /**
     * Sets additional compiler options.
     */
    public void setCompilerOptions(List<String> options) {
        logger.info("Setting compiler options: {}", String.join(" ", options));
        compilerService.setCompilerOptions(options);
    }

    /**
     * Runs a JAR file using the CompilerService.
     */
    public void runJar(File jarFile) {
        logger.info("Running JAR file: {}", jarFile.getAbsolutePath());
        compilerService.runJar(jarFile);
    }

    /**
     * Runs the main method of a Java file using the CompilerService.
     */
    public void runMain(JavaFile javaFile) {
        logger.info("Running main method for Java file: {}", javaFile.getFileName());
        compilerService.runMain(javaFile);
    }

    /**
     * Analyzes cyclomatic complexity of a Java file.
     */
    public io.joshuasalcedo.fx.domain.model.java.ComplexityResult analyzeCyclomaticComplexity(JavaFile javaFile) {
        logger.debug("Analyzing cyclomatic complexity for: {}", javaFile.getFileName());
        io.joshuasalcedo.fx.domain.model.java.ComplexityResult result = 
            javaFileAnalyzerService.cyclomaticComplexityAnalyzer().analyze(javaFile);
        logger.info("Cyclomatic complexity analysis for {}: value={}, threshold={}, isHigh={}", 
                   javaFile.getFileName(), result.value(), result.threshold(), result.isHigh());
        if (result.isHigh()) {
            logger.warn("HIGH CYCLOMATIC COMPLEXITY detected in {}: {} (threshold: {}). Recommendation: {}", 
                       javaFile.getFileName(), result.value(), result.threshold(), result.recommendation());
        }
        return result;
    }

    /**
     * Analyzes cognitive complexity of a Java file.
     */
    public io.joshuasalcedo.fx.domain.model.java.ComplexityResult analyzeCognitiveComplexity(JavaFile javaFile) {
        logger.debug("Analyzing cognitive complexity for: {}", javaFile.getFileName());
        io.joshuasalcedo.fx.domain.model.java.ComplexityResult result = 
            javaFileAnalyzerService.cognitiveComplexityAnalyzer().analyze(javaFile);
        logger.info("Cognitive complexity analysis for {}: value={}, threshold={}, isHigh={}", 
                   javaFile.getFileName(), result.value(), result.threshold(), result.isHigh());
        if (result.isHigh()) {
            logger.warn("HIGH COGNITIVE COMPLEXITY detected in {}: {} (threshold: {}). Recommendation: {}", 
                       javaFile.getFileName(), result.value(), result.threshold(), result.recommendation());
        }
        return result;
    }

    /**
     * Analyzes lines of code metrics for a Java file.
     */
    public io.joshuasalcedo.fx.domain.model.java.LinesOfCodeResult analyzeLinesOfCode(JavaFile javaFile) {
        logger.debug("Analyzing lines of code for: {}", javaFile.getFileName());
        io.joshuasalcedo.fx.domain.model.java.LinesOfCodeResult result = 
            javaFileAnalyzerService.linesOfCodeAnalyzer().analyze(javaFile);
        logger.info("Lines of code analysis for {}: total={}, code={}, comments={}, blank={}, commentRatio={:.2f}%", 
                   javaFile.getFileName(), result.totalLines(), result.codeLines(), 
                   result.commentLines(), result.blankLines(), result.commentRatio() * 100);
        if (result.commentRatio() < 0.1) {
            logger.warn("LOW DOCUMENTATION detected in {}: {:.1f}% comment ratio (recommended: >10%)", 
                       javaFile.getFileName(), result.commentRatio() * 100);
        }
        return result;
    }

    /**
     * Analyzes Halstead complexity metrics for a Java file.
     */
    public io.joshuasalcedo.fx.domain.model.java.HalsteadMetrics analyzeHalsteadMetrics(JavaFile javaFile) {
        logger.debug("Analyzing Halstead metrics for: {}", javaFile.getFileName());
        io.joshuasalcedo.fx.domain.model.java.HalsteadMetrics result = 
            javaFileAnalyzerService.halsteadMetricsAnalyzer().analyze(javaFile);
        logger.info("Halstead metrics for {}: operators={}/{}, operands={}/{}, volume={:.2f}, difficulty={:.2f}, effort={:.2f}", 
                   javaFile.getFileName(), result.uniqueOperators(), result.totalOperators(),
                   result.uniqueOperands(), result.totalOperands(), result.volume(), 
                   result.difficulty(), result.effort());
        if (result.difficulty() > 20) {
            logger.warn("HIGH HALSTEAD DIFFICULTY detected in {}: {:.2f} (consider simplifying)", 
                       javaFile.getFileName(), result.difficulty());
        }
        return result;
    }

    /**
     * Analyzes object-oriented metrics for a Java file.
     */
    public io.joshuasalcedo.fx.domain.model.java.ObjectOrientedMetrics analyzeObjectOrientedMetrics(JavaFile javaFile) {
        logger.debug("Analyzing object-oriented metrics for: {}", javaFile.getFileName());
        return javaFileAnalyzerService.objectOrientedMetricsAnalyzer().analyze(javaFile);
    }

    /**
     * Analyzes method-level metrics for a Java file.
     */
    public List<io.joshuasalcedo.fx.domain.model.java.MethodMetrics> analyzeMethodMetrics(JavaFile javaFile) {
        logger.debug("Analyzing method metrics for: {}", javaFile.getFileName());
        return javaFileAnalyzerService.methodMetricsAnalyzer().analyze(javaFile);
    }

    /**
     * Analyzes maintainability index for a Java file.
     */
    public io.joshuasalcedo.fx.domain.model.java.MaintainabilityIndex analyzeMaintainability(JavaFile javaFile) {
        logger.debug("Analyzing maintainability for: {}", javaFile.getFileName());
        io.joshuasalcedo.fx.domain.model.java.MaintainabilityIndex result = 
            javaFileAnalyzerService.maintainabilityAnalyzer().analyze(javaFile);
        logger.info("Maintainability analysis for {}: index={:.1f}, grade={}, interpretation={}", 
                   javaFile.getFileName(), result.index(), result.grade(), result.interpretation());

        if (logger.isDebugEnabled() && result.contributingFactors() != null) {
            result.contributingFactors().forEach((factor, value) -> 
                logger.debug("Maintainability factor for {}: {}={:.2f}", javaFile.getFileName(), factor, value));
        }

        if (result.index() < 40) {
            logger.warn("LOW MAINTAINABILITY detected in {}: {:.1f} (grade: {}) - {}", 
                       javaFile.getFileName(), result.index(), result.grade(), result.interpretation());
        }
        return result;
    }

    /**
     * Performs comprehensive analysis of a Java file using all available analyzers.
     */
    public io.joshuasalcedo.fx.domain.model.java.CompositeAnalysisResult analyzeComprehensive(JavaFile javaFile) {
        logger.info("Performing comprehensive analysis for: {}", javaFile.getFileName());
        long startTime = System.currentTimeMillis();

        io.joshuasalcedo.fx.domain.model.java.CompositeAnalysisResult result = 
            javaFileAnalyzerService.compositeAnalyzer().analyze(javaFile);

        long analysisTime = System.currentTimeMillis() - startTime;
        logger.info("Comprehensive analysis completed for {} in {}ms", javaFile.getFileName(), analysisTime);

        // Log summary of all metrics
        logger.info("Analysis summary for {}: cyclomaticComplexity={}, cognitiveComplexity={}, " +
                   "totalLines={}, maintainabilityIndex={:.1f} ({}), topComplexMethods={}", 
                   javaFile.getFileName(),
                   result.cyclomaticComplexity() != null ? result.cyclomaticComplexity().value() : "N/A",
                   result.cognitiveComplexity() != null ? result.cognitiveComplexity().value() : "N/A",
                   result.linesOfCode() != null ? result.linesOfCode().totalLines() : "N/A",
                   result.maintainabilityIndex() != null ? result.maintainabilityIndex().index() : "N/A",
                   result.maintainabilityIndex() != null ? result.maintainabilityIndex().grade() : "N/A",
                   result.topComplexMethods() != null ? result.topComplexMethods().size() : 0);

        // Log warnings and suggestions
        if (!result.warnings().isEmpty()) {
            logger.warn("Analysis warnings for {}: {}", javaFile.getFileName(), result.warnings());
        }
        if (!result.suggestions().isEmpty()) {
            logger.info("Analysis suggestions for {}: {}", javaFile.getFileName(), result.suggestions());
        }

        return result;
    }

    /**
     * Checks if a Java file has high complexity using specifications.
     */
    public boolean hasHighComplexity(JavaFile javaFile) {
        logger.debug("Checking high complexity for: {}", javaFile.getFileName());
        boolean result = JavaFileAnalysisSpecifications.hasHighCyclomaticComplexity().isSatisfiedBy(javaFile);
        logger.info("High complexity check for {}: {}", javaFile.getFileName(), 
                   result ? "HAS HIGH COMPLEXITY" : "complexity acceptable");
        return result;
    }

    /**
     * Checks if a Java file needs refactoring based on analysis results and specifications.
     */
    public boolean needsRefactoring(JavaFile javaFile) {
        logger.debug("Checking if file needs refactoring: {}", javaFile.getFileName());
        boolean result = JavaFileAnalysisSpecifications.needsUrgentRefactoring().isSatisfiedBy(javaFile);
        logger.info("Refactoring check for {}: {}", javaFile.getFileName(), 
                   result ? "NEEDS URGENT REFACTORING" : "refactoring not required");
        return result;
    }

    /**
     * Checks if a Java file is well-structured using specifications.
     */
    public boolean isWellStructured(JavaFile javaFile) {
        logger.debug("Checking structure quality for: {}", javaFile.getFileName());
        boolean result = JavaFileAnalysisSpecifications.isWellStructured().isSatisfiedBy(javaFile);
        logger.info("Structure check for {}: {}", javaFile.getFileName(), 
                   result ? "WELL STRUCTURED" : "structure needs improvement");
        return result;
    }

    /**
     * Checks if a Java file has good documentation using specifications.
     */
    public boolean hasGoodDocumentation(JavaFile javaFile) {
        logger.debug("Checking documentation quality for: {}", javaFile.getFileName());
        boolean result = JavaFileAnalysisSpecifications.hasGoodDocumentation().isSatisfiedBy(javaFile);
        logger.info("Documentation check for {}: {}", javaFile.getFileName(), 
                   result ? "WELL DOCUMENTED" : "needs more documentation");
        return result;
    }

    /**
     * Checks if a Java file is high quality using composite specification.
     */
    public boolean isHighQuality(JavaFile javaFile) {
        logger.debug("Checking overall quality for: {}", javaFile.getFileName());
        boolean result = JavaFileAnalysisSpecifications.isHighQuality().isSatisfiedBy(javaFile);
        logger.info("Quality assessment for {}: {}", javaFile.getFileName(), 
                   result ? "HIGH QUALITY CODE" : "quality improvements needed");
        return result;
    }

    /**
     * Checks if a Java file is a test file using specifications.
     */
    public boolean isTestFile(JavaFile javaFile) {
        logger.debug("Checking if test file: {}", javaFile.getFileName());
        boolean result = JavaFileAnalysisSpecifications.isTestFile().isSatisfiedBy(javaFile);
        logger.info("Test file check for {}: {}", javaFile.getFileName(), 
                   result ? "IS TEST FILE" : "is production code");
        return result;
    }

    /**
     * Checks if a Java file is executable (has main method) using specifications.
     */
    public boolean isExecutable(JavaFile javaFile) {
        logger.debug("Checking if executable: {}", javaFile.getFileName());
        boolean result = JavaFileAnalysisSpecifications.isExecutable().isSatisfiedBy(javaFile);
        logger.info("Executable check for {}: {}", javaFile.getFileName(), 
                   result ? "IS EXECUTABLE (has main method)" : "not executable");
        return result;
    }

    /**
     * Gets analysis summary for multiple Java files.
     */
    public List<AnalysisSummary> analyzeMultipleFiles(List<JavaFile> javaFiles) {
        logger.info("Starting batch analysis of {} Java files", javaFiles.size());
        long startTime = System.currentTimeMillis();

        List<AnalysisSummary> summaries = javaFiles.stream()
                .map(file -> {
                    io.joshuasalcedo.fx.domain.model.java.CompositeAnalysisResult analysis = analyzeComprehensive(file);
                    AnalysisSummary summary = new AnalysisSummary(file.getFileName(), analysis);

                    if (summary.needsAttention()) {
                        logger.warn("File {} NEEDS ATTENTION: high complexity or low maintainability", file.getFileName());
                    }

                    return summary;
                })
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;
        long needsAttentionCount = summaries.stream().mapToInt(s -> s.needsAttention() ? 1 : 0).sum();

        logger.info("Batch analysis completed in {}ms: {} files analyzed, {} need attention", 
                   totalTime, summaries.size(), needsAttentionCount);

        if (needsAttentionCount > 0) {
            logger.warn("Files needing attention: {}", 
                       summaries.stream()
                               .filter(AnalysisSummary::needsAttention)
                               .map(AnalysisSummary::fileName)
                               .toList());
        }

        return summaries;
    }

    /**
     * Summary record for analysis results.
     */
    public record AnalysisSummary(
            String fileName,
            io.joshuasalcedo.fx.domain.model.java.CompositeAnalysisResult analysis
    ) {
        public boolean needsAttention() {
            return analysis.cyclomaticComplexity() != null && analysis.cyclomaticComplexity().isHigh() || 
                   analysis.cognitiveComplexity() != null && analysis.cognitiveComplexity().isHigh() ||
                   analysis.maintainabilityIndex() != null && analysis.maintainabilityIndex().index() < 40.0;
        }
    }
}
