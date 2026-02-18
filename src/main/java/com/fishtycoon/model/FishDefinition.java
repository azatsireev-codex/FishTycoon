package com.fishtycoon.model;

import java.util.List;

public record FishDefinition(
        String id,
        String displayName,
        FishRarity rarity,
        double sizeMin,
        double sizeMax,
        double basePrice,
        String description,
        List<String> allowedLocations,
        int customModelData,
        String textureKey
) {}
