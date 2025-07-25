package io.joshuasalcedo.fx.domain.model.java;

import java.util.List;

public record CompositeAnalysisResult(
    ComplexityResult cyclomaticComplexity,
    ComplexityResult cognitiveComplexity,
    LinesOfCodeResult linesOfCode,
    HalsteadMetrics halsteadMetrics,
    ObjectOrientedMetrics objectOrientedMetrics,
    List<MethodMetrics> topComplexMethods,
    MaintainabilityIndex maintainabilityIndex,
    List<String> warnings,
    List<String> suggestions
) {}