package com.fishtycoon.model;

public enum FishRarity {
    COMMON(1.0),
    UNCOMMON(1.25),
    RARE(1.7),
    EPIC(2.5),
    LEGENDARY(4.0),
    MYTHIC(7.0);

    private final double multiplier;

    FishRarity(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
