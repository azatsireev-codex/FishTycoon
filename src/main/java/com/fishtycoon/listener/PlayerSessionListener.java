package com.fishtycoon.listener;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.util.Msg;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSessionListener implements Listener {
    private final FishTycoonPlugin plugin;

    public PlayerSessionListener(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.sessions().load(event.getPlayer().getUniqueId(), () -> {
            PlayerData data = plugin.sessions().get(event.getPlayer().getUniqueId());
            if (!plugin.rods().hasOwnedRod(event.getPlayer())) {
                event.getPlayer().getInventory().addItem(plugin.rods().createStarterRod(event.getPlayer(), data));
                event.getPlayer().sendMessage(Msg.c("<green>You received your FishTycoon starter rod."));
            }
            if (!plugin.backpacks().hasOwnedBackpack(event.getPlayer())) {
                event.getPlayer().getInventory().addItem(plugin.backpacks().createBackpackItem(event.getPlayer(), data));
                event.getPlayer().sendMessage(Msg.c("<green>You received your Fish Backpack."));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.sessions().unload(event.getPlayer().getUniqueId());
    }
}
