package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.BackpackFishEntry;
import com.fishtycoon.model.FishDefinition;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.util.Msg;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BackpackService {
    private final FishTycoonPlugin plugin;

    public BackpackService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createBackpackItem(Player player, PlayerData data) {
        ItemStack backpack = new ItemStack(Material.BUNDLE);
        ItemMeta meta = backpack.getItemMeta();
        meta.displayName(Msg.c("<gradient:#f7971e:#ffd200><bold>Fish Backpack</bold></gradient>"));
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Owner: <white>" + player.getName());
        lore.add("<gray>Backpack level: <white>" + data.getBackpackLevel());
        lore.add("<gray>Capacity: <white>" + capacity(data) + " fish");
        lore.add("<gray>Use <white>/fish backpack <gray>to inspect.");
        lore.add("<red>Fish cannot be extracted.");
        meta.lore(lore.stream().map(Msg::c).toList());
        meta.getPersistentDataContainer().set(plugin.keys().backpackItem, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(plugin.keys().backpackOwner, PersistentDataType.STRING, player.getUniqueId().toString());
        backpack.setItemMeta(meta);
        return backpack;
    }

    public boolean isBackpackItem(ItemStack item, UUID uuid) {
        if (item == null || item.getType() != Material.BUNDLE || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        Byte marker = meta.getPersistentDataContainer().get(plugin.keys().backpackItem, PersistentDataType.BYTE);
        String owner = meta.getPersistentDataContainer().get(plugin.keys().backpackOwner, PersistentDataType.STRING);
        return marker != null && marker == (byte) 1 && owner != null && owner.equals(uuid.toString());
    }

    public boolean hasOwnedBackpack(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isBackpackItem(item, player.getUniqueId())) return true;
        }
        return false;
    }

    public int capacity(PlayerData data) {
        int level = Math.max(1, data.getBackpackLevel());
        return plugin.cfg().backpackBaseCapacity() + (level - 1) * plugin.cfg().backpackCapacityPerLevel();
    }

    public boolean addFish(PlayerData data, String fishId, double size, int amount) {
        if (data.backpackUsed() + amount > capacity(data)) return false;
        double normalizedSize = Math.round(size * 100.0) / 100.0;
        BackpackFishEntry existing = data.getBackpackEntries().stream()
                .filter(e -> e.getFishId().equals(fishId) && Math.abs(e.getSize() - normalizedSize) < 0.00001)
                .findFirst().orElse(null);
        if (existing == null) data.getBackpackEntries().add(new BackpackFishEntry(fishId, normalizedSize, amount));
        else existing.setAmount(existing.getAmount() + amount);
        return true;
    }

    public void openBackpackSummary(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        int used = data.backpackUsed();
        int cap = capacity(data);
        player.sendMessage(Msg.c("<gold>Backpack: <white>Lvl " + data.getBackpackLevel() + " <gray>(" + used + "/" + cap + ")"));
        if (data.getBackpackEntries().isEmpty()) {
            player.sendMessage(Msg.c("<gray>Backpack is empty."));
            return;
        }
        data.getBackpackEntries().stream().limit(10).forEach(entry -> {
            FishDefinition fish = plugin.cfg().getFishDefinitions().get(entry.getFishId());
            String name = fish == null ? entry.getFishId() : fish.displayName();
            player.sendMessage(Msg.c("<gray>- " + name + " <white>x" + entry.getAmount() + " <dark_gray>(" + String.format(Locale.US, "%.2f", entry.getSize()) + " cm)"));
        });
        if (data.getBackpackEntries().size() > 10) player.sendMessage(Msg.c("<gray>... and " + (data.getBackpackEntries().size() - 10) + " more stacks."));
    }

    public boolean upgradeBackpack(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        if (data.getBackpackLevel() >= plugin.cfg().backpackMaxLevel()) {
            player.sendMessage(Msg.c("<red>Backpack is already max level."));
            return false;
        }
        int current = data.getBackpackLevel();
        double cost = plugin.cfg().backpackUpgradeBaseCost() * Math.pow(plugin.cfg().backpackUpgradeCostScale(), current - 1);
        if (plugin.economy().getBalance(player) < cost) {
            player.sendMessage(Msg.c("<red>Not enough money. Need <gold>$" + String.format(Locale.US, "%.2f", cost)));
            return false;
        }
        plugin.economy().withdrawPlayer(player, cost);
        data.setBackpackLevel(current + 1);
        player.sendMessage(Msg.c("<green>Backpack upgraded to level <white>" + data.getBackpackLevel() +
                " <green>(capacity: <white>" + capacity(data) + "<green>)"));
        return true;
    }

    public void resetOnRebirth(PlayerData data) {
        data.setBackpackLevel(1);
        data.getBackpackEntries().clear();
    }

    public void refreshBackpackItem(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isBackpackItem(item, player.getUniqueId())) {
                player.getInventory().setItem(i, createBackpackItem(player, data));
                return;
            }
        }
    }
}
