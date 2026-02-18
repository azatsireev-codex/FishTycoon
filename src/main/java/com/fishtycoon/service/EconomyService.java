package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.BackpackFishEntry;
import com.fishtycoon.model.FishDefinition;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.util.Msg;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EconomyService {
    private final FishTycoonPlugin plugin;

    public EconomyService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public void sellAllFish(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        List<BackpackFishEntry> soldEntries = new ArrayList<>(data.getBackpackEntries());

        double total = 0;
        double best = 0;
        int count = 0;

        for (BackpackFishEntry entry : soldEntries) {
            FishDefinition fish = plugin.cfg().getFishDefinitions().get(entry.getFishId());
            if (fish == null) continue;
            double valueEach = plugin.fish().calculateValue(fish, entry.getSize(), data);
            double stackValue = valueEach * entry.getAmount();
            total += stackValue;
            best = Math.max(best, valueEach);
            count += entry.getAmount();
        }

        if (count > 0) {
            data.getBackpackEntries().clear();
            plugin.economy().depositPlayer(player, total);
            player.sendMessage(Msg.c("<green>Sold <white>" + count + " fish from backpack <green>for <gold>$" + String.format(Locale.US, "%.2f", total) + "<green>. Best sale: <gold>$" + String.format(Locale.US, "%.2f", best)));
            plugin.backpacks().refreshBackpackItem(player);
        } else {
            player.sendMessage(Msg.c("<red>Your backpack has no valid fish to sell."));
        }
    }
}
