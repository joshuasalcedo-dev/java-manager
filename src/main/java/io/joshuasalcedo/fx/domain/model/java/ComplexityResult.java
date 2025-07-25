package io.joshuasalcedo.fx.domain.model.java;

public record ComplexityResult(
    int value,
    boolean isHigh,
    String complexityType,
    int threshold,
    String recommendation
) {
    public ComplexityResult(int value, boolean isHigh, String complexityType, int threshold) {
        this(value, isHigh, complexityType, threshold, 
             isHigh ? "Consider refactoring to reduce complexity" : "Complexity is within acceptable limits");
    }
}

