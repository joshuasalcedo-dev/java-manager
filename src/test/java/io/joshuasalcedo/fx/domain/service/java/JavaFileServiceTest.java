package io.joshuasalcedo.fx.domain.service.java;

import io.joshuasalcedo.fx.domain.model.java.*;
import io.joshuasalcedo.fx.domain.repository.JavaFileRepository;
import io.joshuasalcedo.fx.domain.service.analyzer.Analyzer;
import io.joshuasalcedo.fx.domain.spec.Specification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for JavaFileService using mocked dependencies.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JavaFileServiceTest {

    @Mock
    private JavaFileRepository javaFileRepository;
    
    @Mock
    private RefactorService refactorService;
    
    @Mock
    private CompilerService compilerService;
    
    @Mock
    private JavaFileAnalyzerService javaFileAnalyzerService;
    
    @Mock
    private Analyzer<ComplexityResult, JavaFile> cyclomaticComplexityAnalyzer;
    
    @Mock
    private Analyzer<ComplexityResult, JavaFile> cognitiveComplexityAnalyzer;
    
    @Mock
    private Analyzer<LinesOfCodeResult, JavaFile> linesOfCodeAnalyzer;
    
    @Mock
    private Analyzer<HalsteadMetrics, JavaFile> halsteadMetricsAnalyzer;
    
    @Mock
    private Analyzer<ObjectOrientedMetrics, JavaFile> objectOrientedMetricsAnalyzer;
    
    @Mock
    private Analyzer<List<MethodMetrics>, JavaFile> methodMetricsAnalyzer;
    
    @Mock
    private Analyzer<MaintainabilityIndex, JavaFile> maintainabilityAnalyzer;
    
    @Mock
    private Analyzer<CompositeAnalysisResult, JavaFile> compositeAnalyzer;

    private JavaFileService javaFileService;
    private JavaFile testJavaFile;

    @BeforeEach
    void setUp() {
        javaFileService = new JavaFileService(
            javaFileRepository, 
            refactorService, 
            compilerService, 
            javaFileAnalyzerService
        );
        
        // Create test JavaFile
        testJavaFile = createTestJavaFile();
        
        // Setup analyzer mocks with lenient stubbing
        lenient().when(javaFileAnalyzerService.cyclomaticComplexityAnalyzer()).thenReturn(cyclomaticComplexityAnalyzer);
        lenient().when(javaFileAnalyzerService.cognitiveComplexityAnalyzer()).thenReturn(cognitiveComplexityAnalyzer);
        lenient().when(javaFileAnalyzerService.linesOfCodeAnalyzer()).thenReturn(linesOfCodeAnalyzer);
        lenient().when(javaFileAnalyzerService.halsteadMetricsAnalyzer()).thenReturn(halsteadMetricsAnalyzer);
        lenient().when(javaFileAnalyzerService.objectOrientedMetricsAnalyzer()).thenReturn(objectOrientedMetricsAnalyzer);
        lenient().when(javaFileAnalyzerService.methodMetricsAnalyzer()).thenReturn(methodMetricsAnalyzer);
        lenient().when(javaFileAnalyzerService.maintainabilityAnalyzer()).thenReturn(maintainabilityAnalyzer);
        lenient().when(javaFileAnalyzerService.compositeAnalyzer()).thenReturn(compositeAnalyzer);
    }

    private JavaFile createTestJavaFile() {
        JavaFile javaFile = new JavaFile();
        javaFile.setFileName("TestClass.java");
        javaFile.setFilePath("/src/main/java/TestClass.java");
        javaFile.setPackageName("com.example");
        javaFile.setImports(Arrays.asList("java.util.List", "java.util.ArrayList"));
        javaFile.setFileContent("""
            package com.example;
            
            import java.util.List;
            import java.util.ArrayList;
            
            /**
             * Test class for demonstration.
             */
            public class TestClass {
                private List<String> items = new ArrayList<>();
                
                public void addItem(String item) {
                    items.add(item);
                }
                
                public static void main(String[] args) {
                    TestClass test = new TestClass();
                    test.addItem("Hello");
                }
            }
            """);
        
        // Create type declarations
        JavaFile.Method mainMethod = new JavaFile.Method(
            "main",
            "void",
            List.of(new JavaFile.Parameter("args", "String[]", List.of(), false, false)),
            Set.of(JavaFile.Modifier.PUBLIC, JavaFile.Modifier.STATIC),
            List.of(),
            "TestClass test = new TestClass(); test.addItem(\"Hello\");"
        );
        
        JavaFile.Method addItemMethod = new JavaFile.Method(
            "addItem",
            "void",
            List.of(new JavaFile.Parameter("item", "String", List.of(), false, false)),
            Set.of(JavaFile.Modifier.PUBLIC),
            List.of(),
            "items.add(item);"
        );
        
        JavaFile.Field itemsField = new JavaFile.Field(
            "items",
            "List<String>",
            Set.of(JavaFile.Modifier.PRIVATE),
            List.of(),
            "new ArrayList<>()"
        );
        
        JavaFile.TypeDeclaration typeDeclaration = new JavaFile.TypeDeclaration(
            "TestClass",
            JavaFile.TypeKind.CLASS,
            Set.of(JavaFile.Modifier.PUBLIC),
            List.of("/**\n * Test class for demonstration.\n */"),
            null,
            List.of(),
            List.of(itemsField),
            List.of(),
            List.of(mainMethod, addItemMethod),
            List.of()
        );
        
        javaFile.setTypeDeclarations(List.of(typeDeclaration));
        return javaFile;
    }

    @Test
    void save() {
        // Given
        when(javaFileRepository.save(testJavaFile)).thenReturn(testJavaFile);
        
        // When
        JavaFile result = javaFileService.save(testJavaFile);
        
        // Then
        assertNotNull(result);
        assertEquals(testJavaFile, result);
        verify(javaFileRepository).save(testJavaFile);
    }

    @Test
    void delete() {
        // When
        javaFileService.delete(testJavaFile);
        
        // Then
        verify(javaFileRepository).delete(testJavaFile);
    }

    @Test
    void findAllInDirectory() {
        // Given
        Path directory = Paths.get("/src/main/java");
        List<JavaFile> expectedFiles = List.of(testJavaFile);
        when(javaFileRepository.findAllInDirectory(directory)).thenReturn(expectedFiles);
        
        // When
        List<JavaFile> result = javaFileService.findAllInDirectory(directory);
        
        // Then
        assertEquals(expectedFiles, result);
        verify(javaFileRepository).findAllInDirectory(directory);
    }

    @Test
    void findAllWithSpec() {
        // Given
        Specification<JavaFile> mockSpec = mock(Specification.class);
        List<JavaFile> expectedFiles = List.of(testJavaFile);
        when(javaFileRepository.findBySpecification(mockSpec)).thenReturn(expectedFiles);
        
        // When
        List<JavaFile> result = javaFileService.findAllWithSpec(mockSpec);
        
        // Then
        assertEquals(expectedFiles, result);
        verify(javaFileRepository).findBySpecification(mockSpec);
    }

    @Test
    void compile() {
        // Given
        CompilerService.CompilationResult expectedResult = new CompilerService.CompilationResult(
            true,
            List.of(),
            Optional.of(Paths.get("/target/classes")),
            1000L
        );
        when(compilerService.compile(any(File.class))).thenReturn(expectedResult);
        
        // When
        CompilerService.CompilationResult result = javaFileService.compile(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertTrue(result.success());
        verify(compilerService).compile(any(File.class));
    }

    @Test
    void compileFromSource() {
        // Given
        String sourceCode = "public class Test {}";
        String className = "Test";
        CompilerService.CompilationResult expectedResult = new CompilerService.CompilationResult(
            true,
            List.of(),
            Optional.of(Paths.get("/target/classes")),
            500L
        );
        when(compilerService.compile(sourceCode, className)).thenReturn(expectedResult);
        
        // When
        CompilerService.CompilationResult result = javaFileService.compileFromSource(sourceCode, className);
        
        // Then
        assertEquals(expectedResult, result);
        assertTrue(result.success());
        verify(compilerService).compile(sourceCode, className);
    }

    @Test
    void compileAll() {
        // Given
        List<JavaFile> files = List.of(testJavaFile);
        CompilerService.CompilationResult expectedResult = new CompilerService.CompilationResult(
            true,
            List.of(),
            Optional.of(Paths.get("/target/classes")),
            2000L
        );
        when(compilerService.compile(any(List.class))).thenReturn(expectedResult);
        
        // When
        CompilerService.CompilationResult result = javaFileService.compileAll(files);
        
        // Then
        assertEquals(expectedResult, result);
        assertTrue(result.success());
        verify(compilerService).compile(any(List.class));
    }

    @Test
    void canCompile() {
        // Given
        CompilerService.CompilationResult successResult = new CompilerService.CompilationResult(
            true,
            List.of(),
            Optional.of(Paths.get("/target/classes")),
            1000L
        );
        when(compilerService.compile(any(File.class))).thenReturn(successResult);
        
        // When
        boolean result = javaFileService.canCompile(testJavaFile);
        
        // Then
        assertTrue(result);
        verify(compilerService).compile(any(File.class));
    }

    @Test
    void getDiagnostics() {
        // Given
        List<CompilerService.Diagnostic> expectedDiagnostics = List.of(
            new CompilerService.Diagnostic(
                CompilerService.Severity.WARNING,
                "Unused import",
                Optional.of("TestClass.java"),
                Optional.of(3L),
                Optional.of(1L)
            )
        );
        CompilerService.CompilationResult compilationResult = new CompilerService.CompilationResult(
            true, expectedDiagnostics, Optional.empty(), 1000L
        );
        when(compilerService.compile(any(File.class))).thenReturn(compilationResult);
        
        // When
        List<CompilerService.Diagnostic> result = javaFileService.getDiagnostics(testJavaFile);
        
        // Then
        assertEquals(expectedDiagnostics, result);
        assertEquals(1, result.size());
        verify(compilerService).compile(any(File.class));
    }

    @Test
    void compileDirectory() {
        // Given
        Path directory = Paths.get("/src/main/java");
        CompilerService.CompilationResult expectedResult = new CompilerService.CompilationResult(
            true,
            List.of(),
            Optional.of(Paths.get("/target/classes")),
            3000L
        );
        when(compilerService.compileDirectory(directory, true)).thenReturn(expectedResult);
        
        // When
        CompilerService.CompilationResult result = javaFileService.compileDirectory(directory, true);
        
        // Then
        assertEquals(expectedResult, result);
        verify(compilerService).compileDirectory(directory, true);
    }

    @Test
    void setClasspath() {
        // Given
        List<String> classpath = List.of("/lib/junit.jar", "/lib/mockito.jar");
        
        // When
        javaFileService.setClasspath(classpath);
        
        // Then
        verify(compilerService).setClasspath(classpath);
    }

    @Test
    void setOutputDirectory() {
        // Given
        Path outputDir = Paths.get("/target/classes");
        
        // When
        javaFileService.setOutputDirectory(outputDir);
        
        // Then
        verify(compilerService).setOutputDirectory(outputDir);
    }

    @Test
    void setCompilerOptions() {
        // Given
        List<String> options = List.of("-g", "-verbose");
        
        // When
        javaFileService.setCompilerOptions(options);
        
        // Then
        verify(compilerService).setCompilerOptions(options);
    }

    @Test
    void runJar() {
        // Given
        File jarFile = new File("/path/to/test.jar");
        
        // When
        javaFileService.runJar(jarFile);
        
        // Then
        verify(compilerService).runJar(jarFile);
    }

    @Test
    void runMain() {
        // When
        javaFileService.runMain(testJavaFile);
        
        // Then
        verify(compilerService).runMain(testJavaFile);
    }

    @Test
    void analyzeCyclomaticComplexity() {
        // Given
        ComplexityResult expectedResult = new ComplexityResult(5, false, "Cyclomatic", 10);
        when(cyclomaticComplexityAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        ComplexityResult result = javaFileService.analyzeCyclomaticComplexity(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertFalse(result.isHigh());
        verify(cyclomaticComplexityAnalyzer).analyze(testJavaFile);
    }

    @Test
    void analyzeCognitiveComplexity() {
        // Given
        ComplexityResult expectedResult = new ComplexityResult(3, false, "Cognitive", 8);
        when(cognitiveComplexityAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        ComplexityResult result = javaFileService.analyzeCognitiveComplexity(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertFalse(result.isHigh());
        verify(cognitiveComplexityAnalyzer).analyze(testJavaFile);
    }

    @Test
    void analyzeLinesOfCode() {
        // Given
        LinesOfCodeResult expectedResult = new LinesOfCodeResult(25, 15, 5, 5, 0.2);
        when(linesOfCodeAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        LinesOfCodeResult result = javaFileService.analyzeLinesOfCode(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertEquals(25, result.totalLines());
        assertEquals(0.2, result.commentRatio(), 0.001);
        verify(linesOfCodeAnalyzer).analyze(testJavaFile);
    }

    @Test
    void analyzeHalsteadMetrics() {
        // Given
        HalsteadMetrics expectedResult = new HalsteadMetrics(
            10, 15, 20, 25, 45.0, 25.0, 150.0, 5.0, 750.0, 25.0, 0.05
        );
        when(halsteadMetricsAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        HalsteadMetrics result = javaFileService.analyzeHalsteadMetrics(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertEquals(150.0, result.volume(), 0.001);
        verify(halsteadMetricsAnalyzer).analyze(testJavaFile);
    }

    @Test
    void analyzeObjectOrientedMetrics() {
        // Given
        ObjectOrientedMetrics expectedResult = new ObjectOrientedMetrics(
            1, 0, 2, 0.1, 3, 2, 1
        );
        when(objectOrientedMetricsAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        ObjectOrientedMetrics result = javaFileService.analyzeObjectOrientedMetrics(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertEquals(1, result.depthOfInheritance());
        assertEquals(2, result.numberOfMethods());
        verify(objectOrientedMetricsAnalyzer).analyze(testJavaFile);
    }

    @Test
    void analyzeMethodMetrics() {
        // Given
        List<MethodMetrics> expectedResult = List.of(
            new MethodMetrics("main", 2, 3, 1, 1, true, true, "void"),
            new MethodMetrics("addItem", 1, 1, 1, 0, true, false, "void")
        );
        when(methodMetricsAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        List<MethodMetrics> result = javaFileService.analyzeMethodMetrics(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertEquals(2, result.size());
        assertEquals("main", result.get(0).methodName());
        assertEquals("addItem", result.get(1).methodName());
        verify(methodMetricsAnalyzer).analyze(testJavaFile);
    }

    @Test
    void analyzeMaintainability() {
        // Given
        Map<String, Double> factors = Map.of(
            "complexity", 5.0,
            "documentation", 20.0,
            "size", 25.0
        );
        MaintainabilityIndex expectedResult = new MaintainabilityIndex(
            75.0, "B", "Moderately maintainable", factors
        );
        when(maintainabilityAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        MaintainabilityIndex result = javaFileService.analyzeMaintainability(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertEquals(75.0, result.index(), 0.001);
        assertEquals("B", result.grade());
        verify(maintainabilityAnalyzer).analyze(testJavaFile);
    }

    @Test
    void analyzeComprehensive() {
        // Given
        ComplexityResult cyclomaticResult = new ComplexityResult(5, false, "Cyclomatic", 10);
        ComplexityResult cognitiveResult = new ComplexityResult(3, false, "Cognitive", 8);
        LinesOfCodeResult locResult = new LinesOfCodeResult(25, 15, 5, 5, 0.2);
        HalsteadMetrics halsteadResult = new HalsteadMetrics(10, 15, 20, 25, 45.0, 25.0, 150.0, 5.0, 750.0, 25.0, 0.05);
        ObjectOrientedMetrics ooResult = new ObjectOrientedMetrics(1, 0, 2, 0.1, 3, 2, 1);
        List<MethodMetrics> methodResult = List.of(
            new MethodMetrics("main", 2, 3, 1, 1, true, true, "void")
        );
        MaintainabilityIndex maintainabilityResult = new MaintainabilityIndex(
            75.0, "B", "Moderately maintainable", Map.of()
        );
        
        CompositeAnalysisResult expectedResult = new CompositeAnalysisResult(
            cyclomaticResult,
            cognitiveResult,
            locResult,
            halsteadResult,
            ooResult,
            methodResult,
            maintainabilityResult,
            List.of("Warning: Consider adding more comments"),
            List.of("Suggestion: Break down large methods")
        );
        
        when(compositeAnalyzer.analyze(testJavaFile)).thenReturn(expectedResult);
        
        // When
        CompositeAnalysisResult result = javaFileService.analyzeComprehensive(testJavaFile);
        
        // Then
        assertEquals(expectedResult, result);
        assertNotNull(result.cyclomaticComplexity());
        assertNotNull(result.maintainabilityIndex());
        assertEquals(1, result.warnings().size());
        assertEquals(1, result.suggestions().size());
        verify(compositeAnalyzer).analyze(testJavaFile);
    }

    @Test
    void hasHighComplexity() {
        // When - Uses specification logic based on test file structure
        boolean result = javaFileService.hasHighComplexity(testJavaFile);
        
        // Then - Test file should not have high complexity (simple structure)
        assertFalse(result);
    }

    @Test
    void needsRefactoring() {
        // When - Uses specification logic
        boolean result = javaFileService.needsRefactoring(testJavaFile);
        
        // Then - Test file should not need urgent refactoring
        assertFalse(result);
    }

    @Test
    void isWellStructured() {
        // When
        boolean result = javaFileService.isWellStructured(testJavaFile);
        
        // Then - Test file has 1 type, 2 methods, 2 imports - should be well structured
        assertTrue(result);
    }

    @Test
    void hasGoodDocumentation() {
        // When
        boolean result = javaFileService.hasGoodDocumentation(testJavaFile);
        
        // Then - Test file has comments with good ratio
        assertTrue(result);
    }

    @Test
    void isHighQuality() {
        // When - Uses composite specification
        boolean result = javaFileService.isHighQuality(testJavaFile);
        
        // Then - Test file should be high quality (well structured, documented, follows conventions)
        assertTrue(result);
    }

    @Test
    void isTestFile() {
        // Given - Create a test file
        JavaFile testFile = createTestJavaFile();
        testFile.setFileName("SomethingTest.java");
        
        // When
        boolean result = javaFileService.isTestFile(testFile);
        
        // Then
        assertTrue(result); // File name ends with "Test.java"
    }

    @Test
    void isExecutable() {
        // When
        boolean result = javaFileService.isExecutable(testJavaFile);
        
        // Then
        assertTrue(result); // Test file has a main method
    }

    @Test
    void analyzeMultipleFiles() {
        // Given
        JavaFile file1 = createTestJavaFile();
        file1.setFileName("File1.java");
        JavaFile file2 = createTestJavaFile();
        file2.setFileName("File2.java");
        
        List<JavaFile> files = List.of(file1, file2);
        
        CompositeAnalysisResult analysis1 = mock(CompositeAnalysisResult.class);
        CompositeAnalysisResult analysis2 = mock(CompositeAnalysisResult.class);
        
        when(compositeAnalyzer.analyze(file1)).thenReturn(analysis1);
        when(compositeAnalyzer.analyze(file2)).thenReturn(analysis2);
        
        // When
        List<JavaFileService.AnalysisSummary> result = javaFileService.analyzeMultipleFiles(files);
        
        // Then
        assertEquals(2, result.size());
        assertEquals("File1.java", result.get(0).fileName());
        assertEquals("File2.java", result.get(1).fileName());
        verify(compositeAnalyzer, times(2)).analyze(any(JavaFile.class));
    }

    @Test
    void refactorPackageName() {
        // Given
        String oldPackage = "com.old";
        String newPackage = "com.new";
        RefactorService.RefactorResult expectedResult = mock(RefactorService.RefactorResult.class);
        when(expectedResult.isSuccessful()).thenReturn(true);
        when(expectedResult.getTotalFilesAffected()).thenReturn(5);
        when(refactorService.refactorPackageName(oldPackage, newPackage)).thenReturn(expectedResult);
        
        // When
        RefactorService.RefactorResult result = javaFileService.refactorPackageName(oldPackage, newPackage);
        
        // Then
        assertEquals(expectedResult, result);
        assertTrue(result.isSuccessful());
        verify(refactorService).refactorPackageName(oldPackage, newPackage);
    }

    @Test
    void refactorPackageNameSimple() {
        // Given
        String oldPackage = "com.old";
        String newPackage = "com.new";
        RefactorService.RefactorResult mockResult = mock(RefactorService.RefactorResult.class);
        when(mockResult.getModifiedFiles()).thenReturn(List.of(testJavaFile));
        when(refactorService.refactorPackageName(oldPackage, newPackage)).thenReturn(mockResult);
        
        // When
        List<JavaFile> result = javaFileService.refactorPackageNameSimple(oldPackage, newPackage);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testJavaFile, result.get(0));
        verify(refactorService).refactorPackageName(oldPackage, newPackage);
    }

    @Test
    void refactorTypeName() {
        // Given
        String oldType = "OldClass";
        String newType = "NewClass";
        String packageName = "com.example";
        RefactorService.RefactorResult expectedResult = mock(RefactorService.RefactorResult.class);
        when(refactorService.refactorTypeName(oldType, newType, packageName)).thenReturn(expectedResult);
        
        // When
        RefactorService.RefactorResult result = javaFileService.refactorTypeName(oldType, newType, packageName);
        
        // Then
        assertEquals(expectedResult, result);
        verify(refactorService).refactorTypeName(oldType, newType, packageName);
    }

    @Test
    void refactorMethodName() {
        // Given
        String oldMethod = "oldMethod";
        String newMethod = "newMethod";
        String className = "TestClass";
        RefactorService.RefactorResult expectedResult = mock(RefactorService.RefactorResult.class);
        when(refactorService.refactorMethodName(oldMethod, newMethod, className)).thenReturn(expectedResult);
        
        // When
        RefactorService.RefactorResult result = javaFileService.refactorMethodName(oldMethod, newMethod, className);
        
        // Then
        assertEquals(expectedResult, result);
        verify(refactorService).refactorMethodName(oldMethod, newMethod, className);
    }

    @Test
    void validateRefactoring() {
        // Given
        RefactorService.RefactorOperation operation = mock(RefactorService.RefactorOperation.class);
        RefactorService.RefactorValidationResult expectedResult = mock(RefactorService.RefactorValidationResult.class);
        when(operation.getDescription()).thenReturn("Test operation");
        when(refactorService.validateRefactoring(operation)).thenReturn(expectedResult);
        
        // When
        RefactorService.RefactorValidationResult result = javaFileService.validateRefactoring(operation);
        
        // Then
        assertEquals(expectedResult, result);
        verify(refactorService).validateRefactoring(operation);
    }

    @Test
    void previewRefactoring() {
        // Given
        RefactorService.RefactorOperation operation = mock(RefactorService.RefactorOperation.class);
        RefactorService.RefactorPreviewResult expectedResult = mock(RefactorService.RefactorPreviewResult.class);
        when(operation.getDescription()).thenReturn("Test operation");
        when(refactorService.previewRefactoring(operation)).thenReturn(expectedResult);
        
        // When
        RefactorService.RefactorPreviewResult result = javaFileService.previewRefactoring(operation);
        
        // Then
        assertEquals(expectedResult, result);
        verify(refactorService).previewRefactoring(operation);
    }

    @Test
    void moveTypeToPackage() {
        // Given
        String typeName = "TestClass";
        String fromPackage = "com.old";
        String toPackage = "com.new";
        RefactorService.RefactorResult expectedResult = mock(RefactorService.RefactorResult.class);
        when(refactorService.moveTypeToPackage(typeName, fromPackage, toPackage)).thenReturn(expectedResult);
        
        // When
        RefactorService.RefactorResult result = javaFileService.moveTypeToPackage(typeName, fromPackage, toPackage);
        
        // Then
        assertEquals(expectedResult, result);
        verify(refactorService).moveTypeToPackage(typeName, fromPackage, toPackage);
    }
}