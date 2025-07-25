package io.joshuasalcedo.fx.domain.model.java;

public record LinesOfCodeResult(
    int totalLines,
    int codeLines,
    int commentLines,
    int blankLines,
    double commentRatio
) {}
