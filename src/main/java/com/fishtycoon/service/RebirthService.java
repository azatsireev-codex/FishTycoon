package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.util.Msg;
import org.bukkit.entity.Player;

public class RebirthService {
    private final FishTycoonPlugin plugin;

    public RebirthService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canRebirth(PlayerData data) {
        return data.getRodLevel() >= plugin.cfg().getMaxLevel();
    }

    public void rebirth(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        if (!canRebirth(data)) {
            player.sendMessage(Msg.c("<red>You need rod level " + plugin.cfg().getMaxLevel() + " to rebirth."));
            return;
        }
        data.setRodLevel(1);
        data.setRodXp(0);
        data.setRebirthCount(data.getRebirthCount() + 1);
        plugin.backpacks().resetOnRebirth(data);
        if (plugin.cfg().resetUpgradesOnRebirth()) data.getUpgrades().clear();
        plugin.sessions().save(data);
        plugin.backpacks().refreshBackpackItem(player);
        player.sendMessage(Msg.c("<gold>Rebirth complete! Progress reset, backpack reset, permanent bonuses increased. Total rebirths: <white>" + data.getRebirthCount()));
    }
}
