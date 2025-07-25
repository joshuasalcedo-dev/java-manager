package io.joshuasalcedo.fx.domain.model.java;

import java.util.Map;

public record MaintainabilityIndex(
    double index,
    String grade,
    String interpretation,
    Map<String, Double> contributingFactors
) {
    public static String calculateGrade(double index) {
        if (index >= 80) return "A";
        if (index >= 60) return "B";
        if (index >= 40) return "C";
        if (index >= 20) return "D";
        return "F";
    }
    
    public static String getInterpretation(double index) {
        if (index >= 80) return "Highly maintainable";
        if (index >= 60) return "Moderately maintainable";
        if (index >= 40) return "Difficult to maintain";
        if (index >= 20) return "Very difficult to maintain";
        return "Unmaintainable";
    }
}