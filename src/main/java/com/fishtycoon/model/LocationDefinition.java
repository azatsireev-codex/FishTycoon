package com.fishtycoon.model;

import org.bukkit.Location;

import java.util.List;

public record LocationDefinition(
        String id,
        String displayName,
        String world,
        Location center,
        double radius,
        int requiredLevel,
        List<String> fishPool,
        List<HotspotDefinition> hotspots
) {}
