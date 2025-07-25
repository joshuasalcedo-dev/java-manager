package io.joshuasalcedo.fx.domain.model.java;

public record MethodMetrics(
    String methodName,
    int cyclomaticComplexity,
    int linesOfCode,
    int parameters,
    int localVariables,
    boolean isPublic,
    boolean isStatic,
    String returnType
) {}