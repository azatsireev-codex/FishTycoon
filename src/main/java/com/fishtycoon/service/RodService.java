package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.util.Msg;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RodService {
    private final FishTycoonPlugin plugin;

    public RodService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createStarterRod(Player player, PlayerData data) {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();
        meta.displayName(Msg.c("<gradient:#00d2ff:#3a7bd5><bold>FishTycoon Rod</bold></gradient>"));
        List<String> plainLore = new ArrayList<>();
        plainLore.add("Owner: " + player.getName());
        plainLore.add("Level: " + data.getRodLevel());
        meta.lore(plainLore.stream().map(Msg::c).toList());
        meta.getPersistentDataContainer().set(plugin.keys().rodOwner, PersistentDataType.STRING, player.getUniqueId().toString());
        rod.setItemMeta(meta);
        return rod;
    }

    public boolean isValidRod(ItemStack item, UUID uuid) {
        if (item == null || item.getType() != Material.FISHING_ROD || !item.hasItemMeta()) return false;
        String owner = item.getItemMeta().getPersistentDataContainer().get(plugin.keys().rodOwner, PersistentDataType.STRING);
        return owner != null && owner.equals(uuid.toString());
    }

    public boolean hasOwnedRod(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isValidRod(item, player.getUniqueId())) return true;
        }
        return false;
    }
}
