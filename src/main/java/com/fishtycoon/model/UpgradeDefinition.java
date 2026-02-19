package com.fishtycoon.model;

public record UpgradeDefinition(
        UpgradeType type,
        String displayName,
        String description,
        int maxTier,
        double baseCost,
        double costScale,
        double valuePerTier
) {
    public double costAtTier(int currentTier) {
        return baseCost * Math.pow(costScale, currentTier);
    }
}
