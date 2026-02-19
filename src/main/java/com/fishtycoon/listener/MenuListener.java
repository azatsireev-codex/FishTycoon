package com.fishtycoon.listener;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.model.UpgradeDefinition;
import com.fishtycoon.model.UpgradeType;
import com.fishtycoon.service.MenuService;
import com.fishtycoon.util.Msg;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;

public class MenuListener implements Listener {
    private final FishTycoonPlugin plugin;

    public MenuListener(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle().equals(MenuService.UPGRADES_TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.ENCHANTED_BOOK) return;
            int slot = event.getRawSlot();
            ArrayList<UpgradeDefinition> defs = new ArrayList<>(plugin.cfg().getUpgradeDefinitions().values());
            if (slot < 0 || slot >= defs.size()) return;
            UpgradeDefinition def = defs.get(slot);
            buyUpgrade(player, def.type());
            plugin.menus().openUpgrades(player);
        } else if (event.getView().getTitle().equals(MenuService.BESTIARY_TITLE)) {
            event.setCancelled(true);
        }
    }

    private void buyUpgrade(Player player, UpgradeType type) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        UpgradeDefinition def = plugin.cfg().getUpgradeDefinitions().get(type);
        int tier = data.getUpgradeTier(type);
        if (tier >= def.maxTier()) {
            player.sendMessage(Msg.c("<red>Upgrade already maxed."));
            return;
        }
        double cost = def.costAtTier(tier);
        if (plugin.economy().getBalance(player) < cost) {
            player.sendMessage(Msg.c("<red>Not enough money."));
            return;
        }
        plugin.economy().withdrawPlayer(player, cost);
        data.getUpgrades().put(type, tier + 1);
        player.sendMessage(Msg.c("<green>Upgraded " + def.displayName() + " to tier " + (tier + 1)));
    }
}
