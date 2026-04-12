package com.hound.semantic.model;

public record LineageEdge(
        String sourceGeoid,
        String targetGeoid,
        String relationType,
        String statementGeoid,
        String usageContext
) {}