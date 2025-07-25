package io.joshuasalcedo.fx.domain.service.java;

import io.joshuasalcedo.fx.domain.model.java.*;
import io.joshuasalcedo.fx.domain.service.analyzer.Analyzer;

import java.util.List;
import java.util.Map;

/**
 * Service interface for analyzing Java files using various metrics.
 * Each method returns a specific Analyzer implementation for different aspects of code analysis.
 *
 * @author JoshuaSalcedo
 * @created 7/25/2025
 * @since ${PROJECT.version}
 */
public interface JavaFileAnalyzerService {
    
    /**
     * Returns an analyzer for calculating cyclomatic complexity of Java files.
     * Cyclomatic complexity measures the number of linearly independent paths through code.
     */
    Analyzer<ComplexityResult, JavaFile> cyclomaticComplexityAnalyzer();
    
    /**
     * Returns an analyzer for calculating cognitive complexity of Java files.
     * Cognitive complexity focuses on how difficult code is to understand.
     */
    Analyzer<ComplexityResult, JavaFile> cognitiveComplexityAnalyzer();
    
    /**
     * Returns an analyzer for calculating lines of code metrics.
     * Includes physical LOC, logical LOC, and comment lines.
     */
    Analyzer<LinesOfCodeResult, JavaFile> linesOfCodeAnalyzer();
    
    /**
     * Returns an analyzer for calculating Halstead complexity metrics.
     * Based on the number of operators and operands in the code.
     */
    Analyzer<HalsteadMetrics, JavaFile> halsteadMetricsAnalyzer();
    
    /**
     * Returns an analyzer for object-oriented metrics.
     * Includes coupling, cohesion, inheritance depth, etc.
     */
    Analyzer<ObjectOrientedMetrics, JavaFile> objectOrientedMetricsAnalyzer();
    
    /**
     * Returns an analyzer for method-level metrics.
     * Analyzes individual methods within the Java file.
     */
    Analyzer<List<MethodMetrics>, JavaFile> methodMetricsAnalyzer();
    
    /**
     * Returns an analyzer for code maintainability index.
     * Combines various metrics to produce a single maintainability score.
     */
    Analyzer<MaintainabilityIndex, JavaFile> maintainabilityAnalyzer();
    
    /**
     * Returns a composite analyzer that runs all available analyzers.
     * Provides a comprehensive analysis of the Java file.
     */
    Analyzer<CompositeAnalysisResult, JavaFile> compositeAnalyzer();
}








