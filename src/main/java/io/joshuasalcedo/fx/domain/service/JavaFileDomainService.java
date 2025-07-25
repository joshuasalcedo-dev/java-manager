package io.joshuasalcedo.fx.domain.service;

import io.joshuasalcedo.fx.domain.model.java.JavaFile;
import io.joshuasalcedo.fx.domain.model.java.JavaFile.*;
import io.joshuasalcedo.fx.domain.spec.java.JavaFileSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Domain service for complex Java source file analysis and operations.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public class JavaFileDomainService {

    private static final Logger logger = LoggerFactory.getLogger(JavaFileDomainService.class);

    /**
     * Analyzes dependencies between Java files
     */
    public DependencyAnalysisResult analyzeDependencies(List<JavaFile> javaFiles) {
        logger.debug("Analyzing dependencies for {} Java files", javaFiles.size());
        
        var result = new DependencyAnalysisResult();
        Map<String, JavaFile> typeToFileMap = buildTypeToFileMap(javaFiles);
        
        for (JavaFile file : javaFiles) {
            Set<String> dependencies = findFileDependencies(file, typeToFileMap);
            result.addFileDependencies(file, dependencies);
        }
        
        // Find circular dependencies
        result.setCircularDependencies(findCircularDependencies(result.getDependencyMap()));
        
        logger.debug("Dependency analysis completed: {} files analyzed, {} circular dependencies found",
                javaFiles.size(), result.getCircularDependencies().size());
        
        return result;
    }

    /**
     * Performs code quality analysis on Java files
     */
    public CodeQualityReport analyzeCodeQuality(JavaFile javaFile) {
        logger.debug("Analyzing code quality for file: {}", javaFile.getFileName());
        
        var report = new CodeQualityReport(javaFile.getFileName());
        
        // Check file structure
        if (!JavaFileSpecifications.isValidPublicJavaFile().isSatisfiedBy(javaFile)) {
            report.addIssue(QualityIssue.error("File does not follow Java naming conventions"));
        }
        
        // Check package naming
        if (!JavaFileSpecifications.hasStandardPackageNaming().isSatisfiedBy(javaFile)) {
            report.addIssue(QualityIssue.warning("Package name should be lowercase"));
        }
        
        // Analyze each type declaration
        if (javaFile.getTypeDeclarations() != null) {
            for (TypeDeclaration type : javaFile.getTypeDeclarations()) {
                analyzeTypeQuality(type, report);
            }
        }
        
        // Check for unused imports
        Set<String> unusedImports = findUnusedImports(javaFile);
        for (String unusedImport : unusedImports) {
            report.addIssue(QualityIssue.warning("Unused import: " + unusedImport));
        }
        
        logger.debug("Code quality analysis completed with {} issues", report.getIssues().size());
        return report;
    }

    /**
     * Refactors package name across multiple files
     */
    public RefactoringResult refactorPackageName(List<JavaFile> files, String oldPackage, String newPackage) {
        logger.debug("Refactoring package from {} to {} in {} files", oldPackage, newPackage, files.size());
        
        var result = new RefactoringResult();
        
        for (JavaFile file : files) {
            if (file.getPackageName() != null && file.getPackageName().startsWith(oldPackage)) {
                String updatedPackage = file.getPackageName().replace(oldPackage, newPackage);
                file.setPackageName(updatedPackage);
                result.addRefactoredFile(file);
                
                // Update imports in other files
                updateImportsInOtherFiles(files, file, oldPackage, newPackage);
            }
        }
        
        logger.debug("Package refactoring completed: {} files modified", result.getRefactoredFiles().size());
        return result;
    }

    /**
     * Finds all types that implement or extend a given type
     */
    public TypeHierarchyResult findTypeHierarchy(List<JavaFile> files, String baseTypeName) {
        logger.debug("Finding type hierarchy for base type: {}", baseTypeName);
        
        var result = new TypeHierarchyResult(baseTypeName);
        
        for (JavaFile file : files) {
            if (file.getTypeDeclarations() == null) continue;
            
            for (TypeDeclaration type : file.getTypeDeclarations()) {
                if (isSubtypeOf(type, baseTypeName)) {
                    result.addSubtype(type.name(), file);
                }
            }
        }
        
        logger.debug("Type hierarchy analysis completed: {} subtypes found", result.getSubtypes().size());
        return result;
    }

    /**
     * Generates import optimization suggestions
     */
    public ImportOptimizationResult optimizeImports(JavaFile javaFile) {
        logger.debug("Optimizing imports for file: {}", javaFile.getFileName());
        
        var result = new ImportOptimizationResult();
        
        if (javaFile.getImports() == null) {
            return result;
        }
        
        // Find unused imports
        Set<String> unusedImports = findUnusedImports(javaFile);
        result.setUnusedImports(unusedImports);
        
        // Find wildcard imports that can be specific
        Set<String> wildcardImports = javaFile.getImports().stream()
                .filter(imp -> imp.endsWith(".*"))
                .collect(Collectors.toSet());
        result.setWildcardImports(wildcardImports);
        
        // Suggest static imports for frequently used static members
        Set<String> staticImportCandidates = findStaticImportCandidates(javaFile);
        result.setStaticImportCandidates(staticImportCandidates);
        
        // Generate optimized import list
        List<String> optimizedImports = new ArrayList<>(javaFile.getImports());
        optimizedImports.removeAll(unusedImports);
        optimizedImports.sort(String::compareTo);
        result.setOptimizedImports(optimizedImports);
        
        logger.debug("Import optimization completed: {} unused, {} wildcards, {} static candidates",
                unusedImports.size(), wildcardImports.size(), staticImportCandidates.size());
        
        return result;
    }

    /**
     * Detects code patterns in Java files
     */
    public PatternDetectionResult detectPatterns(List<JavaFile> files) {
        logger.debug("Detecting design patterns in {} files", files.size());
        
        var result = new PatternDetectionResult();
        
        for (JavaFile file : files) {
            // Detect Singleton pattern
            if (isSingletonPattern(file)) {
                result.addPattern("Singleton", file);
            }
            
            // Detect Factory pattern
            if (isFactoryPattern(file)) {
                result.addPattern("Factory", file);
            }
            
            // Detect Builder pattern
            if (isBuilderPattern(file)) {
                result.addPattern("Builder", file);
            }
            
            // Detect Utility class pattern
            if (JavaFileSpecifications.isUtilityClass().isSatisfiedBy(file)) {
                result.addPattern("Utility", file);
            }
        }
        
        logger.debug("Pattern detection completed: {} patterns found", result.getAllPatterns().size());
        return result;
    }

    /**
     * Performs impact analysis for changing a type
     */
    public ImpactAnalysisResult analyzeTypeChangeImpact(List<JavaFile> files, String typeName) {
        logger.debug("Analyzing impact of changing type: {}", typeName);
        
        var result = new ImpactAnalysisResult(typeName);
        
        for (JavaFile file : files) {
            // Check imports
            if (file.getImports() != null && file.getImports().stream().anyMatch(imp -> imp.contains(typeName))) {
                result.addImpactedFile(file, "Import reference");
            }
            
            // Check type declarations
            if (file.getTypeDeclarations() != null) {
                for (TypeDeclaration type : file.getTypeDeclarations()) {
                    if (isTypeReferenced(type, typeName)) {
                        result.addImpactedFile(file, "Type usage in " + type.name());
                    }
                }
            }
        }
        
        logger.debug("Impact analysis completed: {} files impacted", result.getImpactedFiles().size());
        return result;
    }

    // Helper methods

    private Map<String, JavaFile> buildTypeToFileMap(List<JavaFile> files) {
        Map<String, JavaFile> map = new HashMap<>();
        for (JavaFile file : files) {
            if (file.getTypeDeclarations() != null) {
                for (TypeDeclaration type : file.getTypeDeclarations()) {
                    String fullName = file.getPackageName() != null ? 
                            file.getPackageName() + "." + type.name() : type.name();
                    map.put(fullName, file);
                    map.put(type.name(), file); // Also store simple name
                }
            }
        }
        return map;
    }

    private Set<String> findFileDependencies(JavaFile file, Map<String, JavaFile> typeToFileMap) {
        Set<String> dependencies = new HashSet<>();
        
        if (file.getImports() != null) {
            for (String imp : file.getImports()) {
                String typeName = imp.replace(".*", "");
                if (typeToFileMap.containsKey(typeName)) {
                    dependencies.add(typeName);
                }
            }
        }
        
        return dependencies;
    }

    private Set<List<String>> findCircularDependencies(Map<String, Set<String>> dependencyMap) {
        Set<List<String>> circles = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String node : dependencyMap.keySet()) {
            if (!visited.contains(node)) {
                List<String> path = new ArrayList<>();
                findCycles(node, visited, recursionStack, path, dependencyMap, circles);
            }
        }
        
        return circles;
    }

    private boolean findCycles(String node, Set<String> visited, Set<String> recursionStack,
                              List<String> path, Map<String, Set<String>> graph,
                              Set<List<String>> circles) {
        visited.add(node);
        recursionStack.add(node);
        path.add(node);
        
        Set<String> neighbors = graph.getOrDefault(node, Set.of());
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                if (findCycles(neighbor, visited, recursionStack, path, graph, circles)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                // Found a cycle
                int cycleStart = path.indexOf(neighbor);
                List<String> cycle = new ArrayList<>(path.subList(cycleStart, path.size()));
                circles.add(cycle);
            }
        }
        
        path.remove(path.size() - 1);
        recursionStack.remove(node);
        return false;
    }

    private void analyzeTypeQuality(TypeDeclaration type, CodeQualityReport report) {
        // Check class naming conventions
        if (!Character.isUpperCase(type.name().charAt(0))) {
            report.addIssue(QualityIssue.error("Type name should start with uppercase: " + type.name()));
        }
        
        // Check for too many methods (complexity)
        if (type.methods() != null && type.methods().size() > 20) {
            report.addIssue(QualityIssue.warning("Type has too many methods (" + type.methods().size() + 
                    "), consider refactoring"));
        }
        
        // Check for God class anti-pattern
        if (type.fields() != null && type.fields().size() > 15) {
            report.addIssue(QualityIssue.warning("Type has too many fields (" + type.fields().size() + 
                    "), might be a God class"));
        }
    }

    private Set<String> findUnusedImports(JavaFile file) {
        if (file.getImports() == null || file.getFileContent() == null) {
            return Set.of();
        }
        
        Set<String> unusedImports = new HashSet<>(file.getImports());
        String content = file.getFileContent();
        
        // Simple heuristic: check if imported class name appears in the file
        for (String imp : file.getImports()) {
            String className = imp.substring(imp.lastIndexOf('.') + 1);
            if (className.equals("*") || content.contains(className)) {
                unusedImports.remove(imp);
            }
        }
        
        return unusedImports;
    }

    private Set<String> findStaticImportCandidates(JavaFile file) {
        Set<String> candidates = new HashSet<>();
        
        if (file.getFileContent() == null) {
            return candidates;
        }
        
        // Look for repeated static method calls
        Pattern staticCallPattern = Pattern.compile("([A-Z][a-zA-Z0-9]+)\\.([a-z][a-zA-Z0-9]+)\\(");
        var matcher = staticCallPattern.matcher(file.getFileContent());
        
        Map<String, Integer> staticCallCounts = new HashMap<>();
        while (matcher.find()) {
            String call = matcher.group(1) + "." + matcher.group(2);
            staticCallCounts.merge(call, 1, Integer::sum);
        }
        
        // Suggest static import for frequently used static methods (3+ times)
        staticCallCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .forEach(entry -> candidates.add("static " + entry.getKey()));
        
        return candidates;
    }

    private boolean isSubtypeOf(TypeDeclaration type, String baseTypeName) {
        if (type.superClass() != null && type.superClass().contains(baseTypeName)) {
            return true;
        }
        
        if (type.interfaces() != null) {
            return type.interfaces().stream().anyMatch(intf -> intf.contains(baseTypeName));
        }
        
        return false;
    }

    private boolean isTypeReferenced(TypeDeclaration type, String referencedType) {
        // Check superclass
        if (type.superClass() != null && type.superClass().contains(referencedType)) {
            return true;
        }
        
        // Check interfaces
        if (type.interfaces() != null && type.interfaces().stream().anyMatch(i -> i.contains(referencedType))) {
            return true;
        }
        
        // Check fields
        if (type.fields() != null) {
            for (Field field : type.fields()) {
                if (field.type().contains(referencedType)) {
                    return true;
                }
            }
        }
        
        // Check methods
        if (type.methods() != null) {
            for (Method method : type.methods()) {
                if (method.returnType().contains(referencedType)) {
                    return true;
                }
                if (method.parameters() != null) {
                    for (Parameter param : method.parameters()) {
                        if (param.type().contains(referencedType)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    private void updateImportsInOtherFiles(List<JavaFile> files, JavaFile refactoredFile, 
                                         String oldPackage, String newPackage) {
        String oldImportPrefix = oldPackage + "." + getPublicTypeName(refactoredFile);
        String newImportPrefix = newPackage + "." + getPublicTypeName(refactoredFile);
        
        for (JavaFile file : files) {
            if (file.equals(refactoredFile) || file.getImports() == null) continue;
            
            List<String> updatedImports = file.getImports().stream()
                    .map(imp -> imp.startsWith(oldImportPrefix) ? 
                            imp.replace(oldImportPrefix, newImportPrefix) : imp)
                    .collect(Collectors.toList());
            
            file.setImports(updatedImports);
        }
    }

    private String getPublicTypeName(JavaFile file) {
        if (file.getTypeDeclarations() == null) return "";
        
        return file.getTypeDeclarations().stream()
                .filter(type -> type.modifiers() != null && type.modifiers().contains(Modifier.PUBLIC))
                .map(TypeDeclaration::name)
                .findFirst()
                .orElse("");
    }

    private boolean isSingletonPattern(JavaFile file) {
        if (file.getTypeDeclarations() == null) return false;
        
        return file.getTypeDeclarations().stream().anyMatch(type -> {
            if (type.kind() != TypeKind.CLASS) return false;
            
            boolean hasPrivateConstructor = type.constructors() != null &&
                    type.constructors().stream().anyMatch(c -> 
                            c.modifiers().contains(Modifier.PRIVATE));
            
            boolean hasStaticInstance = type.fields() != null &&
                    type.fields().stream().anyMatch(f -> 
                            f.modifiers().contains(Modifier.STATIC) &&
                            f.type().equals(type.name()));
            
            boolean hasGetInstance = type.methods() != null &&
                    type.methods().stream().anyMatch(m -> 
                            m.name().equals("getInstance") &&
                            m.modifiers().contains(Modifier.STATIC) &&
                            m.returnType().equals(type.name()));
            
            return hasPrivateConstructor && hasStaticInstance && hasGetInstance;
        });
    }

    private boolean isFactoryPattern(JavaFile file) {
        if (file.getTypeDeclarations() == null) return false;
        
        return file.getTypeDeclarations().stream().anyMatch(type -> {
            if (type.methods() == null) return false;
            
            return type.methods().stream().anyMatch(m -> 
                    (m.name().startsWith("create") || m.name().startsWith("make") || 
                     m.name().equals("getInstance")) &&
                    m.modifiers().contains(Modifier.STATIC));
        });
    }

    private boolean isBuilderPattern(JavaFile file) {
        if (file.getTypeDeclarations() == null) return false;
        
        return file.getTypeDeclarations().stream().anyMatch(type -> {
            // Check for inner Builder class
            if (type.nestedTypes() == null) return false;
            
            return type.nestedTypes().stream().anyMatch(nested -> 
                    nested.name().equals("Builder") &&
                    nested.methods() != null &&
                    nested.methods().stream().anyMatch(m -> m.name().equals("build")));
        });
    }

    // Result classes

    public static class DependencyAnalysisResult {
        private final Map<String, Set<String>> dependencyMap = new HashMap<>();
        private Set<List<String>> circularDependencies = new HashSet<>();
        
        public void addFileDependencies(JavaFile file, Set<String> dependencies) {
            dependencyMap.put(file.getFileName(), dependencies);
        }
        
        public Map<String, Set<String>> getDependencyMap() { return dependencyMap; }
        public Set<List<String>> getCircularDependencies() { return circularDependencies; }
        public void setCircularDependencies(Set<List<String>> circularDependencies) {
            this.circularDependencies = circularDependencies;
        }
    }

    public static class CodeQualityReport {
        private final String fileName;
        private final List<QualityIssue> issues = new ArrayList<>();
        
        public CodeQualityReport(String fileName) {
            this.fileName = fileName;
        }
        
        public void addIssue(QualityIssue issue) { issues.add(issue); }
        public String getFileName() { return fileName; }
        public List<QualityIssue> getIssues() { return List.copyOf(issues); }
        public boolean hasErrors() { 
            return issues.stream().anyMatch(i -> i.severity() == QualityIssue.Severity.ERROR);
        }
    }

    public record QualityIssue(Severity severity, String message) {
        public enum Severity { ERROR, WARNING, INFO }
        
        public static QualityIssue error(String message) { return new QualityIssue(Severity.ERROR, message); }
        public static QualityIssue warning(String message) { return new QualityIssue(Severity.WARNING, message); }
        public static QualityIssue info(String message) { return new QualityIssue(Severity.INFO, message); }
    }

    public static class RefactoringResult {
        private final List<JavaFile> refactoredFiles = new ArrayList<>();
        
        public void addRefactoredFile(JavaFile file) { refactoredFiles.add(file); }
        public List<JavaFile> getRefactoredFiles() { return List.copyOf(refactoredFiles); }
    }

    public static class TypeHierarchyResult {
        private final String baseType;
        private final Map<String, JavaFile> subtypes = new HashMap<>();
        
        public TypeHierarchyResult(String baseType) {
            this.baseType = baseType;
        }
        
        public void addSubtype(String typeName, JavaFile file) { subtypes.put(typeName, file); }
        public String getBaseType() { return baseType; }
        public Map<String, JavaFile> getSubtypes() { return Map.copyOf(subtypes); }
    }

    public static class ImportOptimizationResult {
        private Set<String> unusedImports = new HashSet<>();
        private Set<String> wildcardImports = new HashSet<>();
        private Set<String> staticImportCandidates = new HashSet<>();
        private List<String> optimizedImports = new ArrayList<>();
        
        // Getters and setters
        public Set<String> getUnusedImports() { return unusedImports; }
        public void setUnusedImports(Set<String> unusedImports) { this.unusedImports = unusedImports; }
        public Set<String> getWildcardImports() { return wildcardImports; }
        public void setWildcardImports(Set<String> wildcardImports) { this.wildcardImports = wildcardImports; }
        public Set<String> getStaticImportCandidates() { return staticImportCandidates; }
        public void setStaticImportCandidates(Set<String> candidates) { this.staticImportCandidates = candidates; }
        public List<String> getOptimizedImports() { return optimizedImports; }
        public void setOptimizedImports(List<String> optimizedImports) { this.optimizedImports = optimizedImports; }
    }

    public static class PatternDetectionResult {
        private final Map<String, List<JavaFile>> patternOccurrences = new HashMap<>();
        
        public void addPattern(String patternName, JavaFile file) {
            patternOccurrences.computeIfAbsent(patternName, k -> new ArrayList<>()).add(file);
        }
        
        public Map<String, List<JavaFile>> getAllPatterns() { return Map.copyOf(patternOccurrences); }
        public List<JavaFile> getFilesWithPattern(String pattern) { 
            return List.copyOf(patternOccurrences.getOrDefault(pattern, List.of()));
        }
    }

    public static class ImpactAnalysisResult {
        private final String changedType;
        private final Map<JavaFile, String> impactedFiles = new HashMap<>();
        
        public ImpactAnalysisResult(String changedType) {
            this.changedType = changedType;
        }
        
        public void addImpactedFile(JavaFile file, String reason) {
            impactedFiles.put(file, reason);
        }
        
        public String getChangedType() { return changedType; }
        public Map<JavaFile, String> getImpactedFiles() { return Map.copyOf(impactedFiles); }
    }
}