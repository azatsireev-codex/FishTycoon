package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.HotspotDefinition;
import com.fishtycoon.model.LocationDefinition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class LocationService {
    private final FishTycoonPlugin plugin;

    public LocationService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public LocationDefinition currentLocation(Player player) {
        Location loc = player.getLocation();
        return plugin.cfg().getLocationDefinitions().values().stream()
                .filter(l -> l.world().equals(loc.getWorld().getName()) && l.center().distanceSquared(loc) <= l.radius() * l.radius())
                .min(Comparator.comparingDouble(l -> l.center().distanceSquared(loc)))
                .orElse(null);
    }

    public HotspotDefinition currentHotspot(LocationDefinition location, Player player) {
        if (location == null) return null;
        Location loc = player.getLocation();
        List<HotspotDefinition> hotspots = location.hotspots();
        return hotspots.stream()
                .filter(h -> h.center().distanceSquared(loc) <= h.radius() * h.radius())
                .findFirst().orElse(null);
    }
}
