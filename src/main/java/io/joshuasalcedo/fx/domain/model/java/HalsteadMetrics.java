package io.joshuasalcedo.fx.domain.model.java;

public record HalsteadMetrics(
    int uniqueOperators,
    int uniqueOperands,
    int totalOperators,
    int totalOperands,
    double programLength,
    double vocabulary,
    double volume,
    double difficulty,
    double effort,
    double timeToProgram,
    double deliveredBugs
) {}