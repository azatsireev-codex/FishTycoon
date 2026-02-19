package com.fishtycoon.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private int rodLevel;
    private double rodXp;
    private int rebirthCount;
    private long lastRodClaimAt;
    private int backpackLevel;
    private final List<BackpackFishEntry> backpackEntries = new ArrayList<>();
    private final Map<UpgradeType, Integer> upgrades = new EnumMap<>(UpgradeType.class);
    private final Map<String, PlayerFishStat> bestiary = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.rodLevel = 1;
        this.backpackLevel = 1;
    }

    public UUID getUuid() { return uuid; }
    public int getRodLevel() { return rodLevel; }
    public void setRodLevel(int rodLevel) { this.rodLevel = rodLevel; }
    public double getRodXp() { return rodXp; }
    public void setRodXp(double rodXp) { this.rodXp = rodXp; }
    public int getRebirthCount() { return rebirthCount; }
    public void setRebirthCount(int rebirthCount) { this.rebirthCount = rebirthCount; }
    public long getLastRodClaimAt() { return lastRodClaimAt; }
    public void setLastRodClaimAt(long lastRodClaimAt) { this.lastRodClaimAt = lastRodClaimAt; }
    public Map<UpgradeType, Integer> getUpgrades() { return upgrades; }
    public Map<String, PlayerFishStat> getBestiary() { return bestiary; }
    public int getBackpackLevel() { return backpackLevel; }
    public void setBackpackLevel(int backpackLevel) { this.backpackLevel = backpackLevel; }
    public List<BackpackFishEntry> getBackpackEntries() { return backpackEntries; }

    public int getUpgradeTier(UpgradeType type) {
        return upgrades.getOrDefault(type, 0);
    }

    public int backpackUsed() {
        return backpackEntries.stream().mapToInt(BackpackFishEntry::getAmount).sum();
    }
}
