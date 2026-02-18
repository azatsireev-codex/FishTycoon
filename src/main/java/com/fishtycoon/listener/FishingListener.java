package com.fishtycoon.listener;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.*;
import com.fishtycoon.util.Msg;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishingListener implements Listener {
    private final FishTycoonPlugin plugin;

    public FishingListener(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player player = event.getPlayer();
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        if (!plugin.rods().isValidRod(player.getInventory().getItemInMainHand(), player.getUniqueId())) return;

        LocationDefinition location = plugin.locations().currentLocation(player);
        HotspotDefinition hotspot = plugin.locations().currentHotspot(location, player);
        FishDefinition fish = plugin.fish().rollFish(player, data, location, hotspot);
        if (fish == null) return;

        double size = plugin.fish().rollSize(fish, data);
        if (!plugin.backpacks().addFish(data, fish.id(), size, 1)) {
            player.sendMessage(Msg.c("<red>Your backpack is full. Upgrade it with <white>/fish backpack upgrade"));
            return;
        }

        if (plugin.cfg().replaceVanillaLoot() && event.getCaught() instanceof Item caughtItem) {
            caughtItem.remove();
        }

        if (data.getUpgradeTier(UpgradeType.DOUBLE_CATCH) > 0 && Math.random() < 0.04 * data.getUpgradeTier(UpgradeType.DOUBLE_CATCH)) {
            if (plugin.backpacks().addFish(data, fish.id(), size, 1)) {
                player.sendMessage(Msg.c("<aqua>Double Catch activated!"));
            }
        }

        addXp(player, data, plugin.fish().xpReward(fish, data));
        PlayerFishStat stat = data.getBestiary().computeIfAbsent(fish.id(), k -> new PlayerFishStat());
        stat.setTimesCaught(stat.getTimesCaught() + 1);
        stat.setLargestSize(Math.max(stat.getLargestSize(), size));
        if (stat.getFirstCaughtAt() == 0) stat.setFirstCaughtAt(System.currentTimeMillis());

        int used = data.backpackUsed();
        int capacity = plugin.backpacks().capacity(data);
        player.sendMessage(Msg.c("<green>Caught " + fish.displayName() + " <gray>(" + String.format("%.2f", size) + " cm) <dark_gray>[Backpack " + used + "/" + capacity + "]"));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }

    private void addXp(Player player, PlayerData data, double xp) {
        data.setRodXp(data.getRodXp() + xp);
        while (data.getRodLevel() < plugin.cfg().getMaxLevel() && data.getRodXp() >= plugin.cfg().xpForNextLevel(data.getRodLevel())) {
            data.setRodXp(data.getRodXp() - plugin.cfg().xpForNextLevel(data.getRodLevel()));
            data.setRodLevel(data.getRodLevel() + 1);
            player.sendMessage(Msg.c("<gold>Rod leveled up to <white>" + data.getRodLevel()));
        }
        plugin.backpacks().refreshBackpackItem(player);
    }
}
