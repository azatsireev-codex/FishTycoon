package com.fishtycoon.listener;

import com.fishtycoon.FishTycoonPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class BackpackItemListener implements Listener {
    private final FishTycoonPlugin plugin;

    public BackpackItemListener(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (plugin.backpacks().isBackpackItem(current, player.getUniqueId()) || plugin.backpacks().isBackpackItem(cursor, player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (plugin.backpacks().isBackpackItem(event.getOldCursor(), player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.backpacks().isBackpackItem(event.getItemDrop().getItemStack(), event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
