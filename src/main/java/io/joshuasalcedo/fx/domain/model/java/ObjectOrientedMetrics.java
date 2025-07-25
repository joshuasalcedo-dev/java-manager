package io.joshuasalcedo.fx.domain.model.java;

public record ObjectOrientedMetrics(
    int depthOfInheritance,
    int numberOfChildren,
    int couplingBetweenObjects,
    double lackOfCohesion,
    int weightedMethodsPerClass,
    int numberOfMethods,
    int numberOfFields
) {}
