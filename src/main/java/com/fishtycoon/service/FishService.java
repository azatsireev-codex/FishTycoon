package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.*;
import com.fishtycoon.util.Msg;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class FishService {
    private final FishTycoonPlugin plugin;
    private final Random random = new Random();

    public FishService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public FishDefinition rollFish(Player player, PlayerData data, LocationDefinition location, HotspotDefinition hotspot) {
        List<String> poolIds = new ArrayList<>();
        if (hotspot != null && !hotspot.specialPool().isEmpty()) poolIds.addAll(hotspot.specialPool());
        else if (location != null) poolIds.addAll(location.fishPool());
        if (poolIds.isEmpty()) poolIds.addAll(plugin.cfg().getFishDefinitions().keySet());

        List<FishDefinition> pool = poolIds.stream().map(id -> plugin.cfg().getFishDefinitions().get(id)).filter(Objects::nonNull).toList();
        if (pool.isEmpty()) return null;

        double rareBonus = data.getUpgradeTier(UpgradeType.RARE_CHANCE) * plugin.cfg().getUpgradeDefinitions().get(UpgradeType.RARE_CHANCE).valuePerTier();
        rareBonus += Math.min(plugin.cfg().rebirthBonusCap(), data.getRebirthCount() * plugin.cfg().rebirthRareBonusPer());
        if (hotspot != null) rareBonus += hotspot.rareBoost();

        List<FishDefinition> weighted = new ArrayList<>();
        for (FishDefinition def : pool) {
            double w = Math.max(1.0, 100.0 / def.rarity().multiplier() + (def.rarity().ordinal() >= FishRarity.RARE.ordinal() ? -rareBonus * 100 : rareBonus * 40));
            for (int i = 0; i < (int) Math.max(1, w); i++) weighted.add(def);
        }
        return weighted.get(random.nextInt(weighted.size()));
    }

    public double rollSize(FishDefinition fish, PlayerData data) {
        double mean = (fish.sizeMin() + fish.sizeMax()) / 2.0;
        double stdDev = (fish.sizeMax() - fish.sizeMin()) / 6.0;
        double gaussian = mean + random.nextGaussian() * stdDev;
        double sizeBoost = data.getUpgradeTier(UpgradeType.SIZE_BOOST) * plugin.cfg().getUpgradeDefinitions().get(UpgradeType.SIZE_BOOST).valuePerTier();
        gaussian *= (1.0 + sizeBoost);
        return Math.max(fish.sizeMin(), Math.min(fish.sizeMax(), gaussian));
    }

    public ItemStack createFishItem(FishDefinition fish, double size, double value) {
        ItemStack item = new ItemStack(Material.COD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Msg.c(fish.displayName()));
        List<String> lore = List.of(
                "<gray>Rarity: <white>" + fish.rarity().name(),
                "<gray>Size: <white>" + String.format(Locale.US, "%.2f cm", size),
                "<gray>Estimated Value: <gold>$" + String.format(Locale.US, "%.2f", value),
                "<yellow>Sell at Fish Shop"
        );
        meta.lore(lore.stream().map(Msg::c).toList());
        if (fish.customModelData() > 0) meta.setCustomModelData(fish.customModelData());
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(plugin.keys().fishId, PersistentDataType.STRING, fish.id());
        pdc.set(plugin.keys().fishSize, PersistentDataType.DOUBLE, size);
        pdc.set(plugin.keys().fishRarity, PersistentDataType.STRING, fish.rarity().name());
        item.setItemMeta(meta);
        return item;
    }

    public boolean isValidFish(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String fishId = pdc.get(plugin.keys().fishId, PersistentDataType.STRING);
        String rarity = pdc.get(plugin.keys().fishRarity, PersistentDataType.STRING);
        return fishId != null && rarity != null && plugin.cfg().getFishDefinitions().containsKey(fishId);
    }

    public double calculateValue(FishDefinition fish, double size, PlayerData data) {
        double sizeNormalized = (size - fish.sizeMin()) / Math.max(0.001, fish.sizeMax() - fish.sizeMin());
        double sizeMultiplier = 0.8 + sizeNormalized * 0.6;
        double sellBonus = data.getUpgradeTier(UpgradeType.SELL_BONUS) * plugin.cfg().getUpgradeDefinitions().get(UpgradeType.SELL_BONUS).valuePerTier();
        sellBonus += Math.min(plugin.cfg().rebirthBonusCap(), data.getRebirthCount() * plugin.cfg().rebirthSellBonusPer());
        return fish.basePrice() * fish.rarity().multiplier() * sizeMultiplier * (1 + sellBonus);
    }

    public double xpReward(FishDefinition fish, PlayerData data) {
        double xp = plugin.cfg().getBaseXpPerCatch() * fish.rarity().multiplier();
        double xpBonus = data.getUpgradeTier(UpgradeType.XP_BOOST) * plugin.cfg().getUpgradeDefinitions().get(UpgradeType.XP_BOOST).valuePerTier();
        return xp * (1 + xpBonus);
    }
}
