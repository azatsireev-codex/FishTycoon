package com.fishtycoon.model;

import org.bukkit.Location;

import java.util.List;

public record HotspotDefinition(
        String id,
        String displayName,
        Location center,
        double radius,
        double rareBoost,
        List<String> specialPool
) {}
