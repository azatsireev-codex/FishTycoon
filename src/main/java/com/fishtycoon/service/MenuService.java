package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.*;
import com.fishtycoon.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.*;

public class MenuService {
    private final FishTycoonPlugin plugin;
    public static final String UPGRADES_TITLE = "FishTycoon Upgrades";
    public static final String BESTIARY_TITLE = "FishTycoon Bestiary";

    public MenuService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public void openUpgrades(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, UPGRADES_TITLE);
        int slot = 0;
        for (UpgradeDefinition def : plugin.cfg().getUpgradeDefinitions().values()) {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = item.getItemMeta();
            int tier = data.getUpgradeTier(def.type());
            meta.displayName(Msg.c("<aqua>" + def.displayName() + " <gray>[" + tier + "/" + def.maxTier() + "]"));
            double cost = def.costAtTier(tier);
            meta.lore(List.of(
                    Msg.c("<gray>" + def.description()),
                    Msg.c("<gray>Next tier cost: <gold>$" + String.format("%.2f", cost)),
                    Msg.c("<yellow>Click to upgrade")
            ));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openBestiary(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, BESTIARY_TITLE);
        int slot = 0;
        for (FishDefinition fish : plugin.cfg().getFishDefinitions().values()) {
            PlayerFishStat stat = data.getBestiary().get(fish.id());
            ItemStack item = new ItemStack(stat == null ? Material.GRAY_DYE : Material.TROPICAL_FISH);
            ItemMeta meta = item.getItemMeta();
            if (stat == null) {
                meta.displayName(Msg.c("<dark_gray>???"));
                meta.lore(List.of(Msg.c("<gray>Catch this fish to discover it.")));
            } else {
                meta.displayName(Msg.c(fish.displayName()));
                meta.lore(List.of(
                        Msg.c("<gray>Rarity: <white>" + fish.rarity().name()),
                        Msg.c("<gray>Description: <white>" + fish.description()),
                        Msg.c("<gray>Caught: <white>" + stat.getTimesCaught() + " times"),
                        Msg.c("<gray>Largest size: <white>" + String.format(Locale.US, "%.2f cm", stat.getLargestSize())),
                        Msg.c("<gray>First caught: <white>" + Instant.ofEpochMilli(stat.getFirstCaughtAt()))
                ));
            }
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
            if (slot >= inv.getSize()) break;
        }
        player.openInventory(inv);
    }
}
