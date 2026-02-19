package com.fishtycoon.config;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigManager {
    private final FishTycoonPlugin plugin;
    private final Map<String, FishDefinition> fishDefinitions = new HashMap<>();
    private final Map<String, LocationDefinition> locationDefinitions = new HashMap<>();
    private final Map<UpgradeType, UpgradeDefinition> upgradeDefinitions = new EnumMap<>(UpgradeType.class);

    public ConfigManager(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        plugin.saveDefaultConfig();
        plugin.saveResource("fish.yml", false);
        plugin.saveResource("locations.yml", false);
        plugin.reloadConfig();
        loadFish();
        loadLocations();
        loadUpgrades();
    }

    private void loadFish() {
        fishDefinitions.clear();
        File file = new File(plugin.getDataFolder(), "fish.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("fish");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection fish = section.getConfigurationSection(id);
            if (fish == null) continue;
            FishDefinition def = new FishDefinition(
                    id,
                    fish.getString("displayName", id),
                    FishRarity.valueOf(fish.getString("rarity", "COMMON").toUpperCase(Locale.ROOT)),
                    fish.getDouble("sizeMin", 10),
                    fish.getDouble("sizeMax", 30),
                    fish.getDouble("basePrice", 10),
                    fish.getString("description", ""),
                    fish.getStringList("allowedLocations"),
                    fish.getInt("customModelData", 0),
                    fish.getString("textureKey", "")
            );
            fishDefinitions.put(id, def);
        }
    }

    private void loadLocations() {
        locationDefinitions.clear();
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "locations.yml"));
        ConfigurationSection section = cfg.getConfigurationSection("locations");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection loc = section.getConfigurationSection(id);
            if (loc == null) continue;
            World world = Bukkit.getWorld(loc.getString("world", "world"));
            if (world == null) continue;
            Location center = new Location(world, loc.getDouble("center.x"), loc.getDouble("center.y"), loc.getDouble("center.z"));
            List<HotspotDefinition> hotspots = new ArrayList<>();
            ConfigurationSection hotspotSec = loc.getConfigurationSection("hotspots");
            if (hotspotSec != null) {
                for (String hId : hotspotSec.getKeys(false)) {
                    ConfigurationSection hs = hotspotSec.getConfigurationSection(hId);
                    if (hs == null) continue;
                    Location hCenter = new Location(world, hs.getDouble("center.x"), hs.getDouble("center.y"), hs.getDouble("center.z"));
                    hotspots.add(new HotspotDefinition(
                            hId,
                            hs.getString("displayName", hId),
                            hCenter,
                            hs.getDouble("radius", 12),
                            hs.getDouble("rareBoost", 0.02),
                            hs.getStringList("specialPool")
                    ));
                }
            }
            locationDefinitions.put(id, new LocationDefinition(
                    id,
                    loc.getString("displayName", id),
                    world.getName(),
                    center,
                    loc.getDouble("radius", 120),
                    loc.getInt("requiredLevel", 1),
                    loc.getStringList("fishPool"),
                    hotspots
            ));
        }
    }

    private void loadUpgrades() {
        upgradeDefinitions.clear();
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection section = cfg.getConfigurationSection("upgrades");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            UpgradeType type = UpgradeType.valueOf(key.toUpperCase(Locale.ROOT));
            ConfigurationSection us = section.getConfigurationSection(key);
            if (us == null) continue;
            upgradeDefinitions.put(type, new UpgradeDefinition(
                    type,
                    us.getString("name", type.name()),
                    us.getString("description", ""),
                    us.getInt("maxTier", 10),
                    us.getDouble("baseCost", 100),
                    us.getDouble("costScale", 1.4),
                    us.getDouble("valuePerTier", 0.01)
            ));
        }
    }

    public Map<String, FishDefinition> getFishDefinitions() { return fishDefinitions; }
    public Map<String, LocationDefinition> getLocationDefinitions() { return locationDefinitions; }
    public Map<UpgradeType, UpgradeDefinition> getUpgradeDefinitions() { return upgradeDefinitions; }

    public int getMaxLevel() { return plugin.getConfig().getInt("progression.maxLevel", 50); }
    public double getBaseXpPerCatch() { return plugin.getConfig().getDouble("progression.baseXpPerCatch", 10); }
    public long getRodClaimCooldownMs() { return plugin.getConfig().getLong("cooldowns.rodClaimSeconds", 300) * 1000L; }
    public boolean resetUpgradesOnRebirth() { return plugin.getConfig().getBoolean("rebirth.resetUpgrades", true); }
    public double rebirthSellBonusPer() { return plugin.getConfig().getDouble("rebirth.sellBonusPerRebirth", 0.01); }
    public double rebirthRareBonusPer() { return plugin.getConfig().getDouble("rebirth.rareBonusPerRebirth", 0.005); }
    public double rebirthBonusCap() { return plugin.getConfig().getDouble("rebirth.bonusCap", 1.0); }
    public boolean replaceVanillaLoot() { return plugin.getConfig().getBoolean("fishing.replaceVanillaLoot", true); }


    public int backpackBaseCapacity() { return plugin.getConfig().getInt("backpack.baseCapacity", 30); }
    public int backpackCapacityPerLevel() { return plugin.getConfig().getInt("backpack.capacityPerLevel", 15); }
    public int backpackMaxLevel() { return plugin.getConfig().getInt("backpack.maxLevel", 20); }
    public double backpackUpgradeBaseCost() { return plugin.getConfig().getDouble("backpack.upgradeBaseCost", 500); }
    public double backpackUpgradeCostScale() { return plugin.getConfig().getDouble("backpack.upgradeCostScale", 1.35); }

    public double xpForNextLevel(int level) {
        return plugin.getConfig().getDouble("progression.baseXpPerLevel", 100) * Math.pow(1.25, Math.max(0, level - 1));
    }
}
