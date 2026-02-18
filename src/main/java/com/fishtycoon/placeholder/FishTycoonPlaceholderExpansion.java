package com.fishtycoon.placeholder;

import com.fishtycoon.FishTycoonPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class FishTycoonPlaceholderExpansion extends PlaceholderExpansion {
    private final FishTycoonPlugin plugin;

    public FishTycoonPlaceholderExpansion(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "fishtycoon";
    }

    @Override
    public String getAuthor() {
        return "Codex";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params == null || params.isBlank()) return "";

        if (params.equalsIgnoreCase("leaderboards_refresh")) {
            plugin.leaderboards().refreshAll();
            return "ok";
        }

        String[] parts = params.split("_");
        if (parts.length >= 4 && parts[0].equalsIgnoreCase("top")) {
            String type = parts[1].toLowerCase();
            int place;
            try {
                place = Integer.parseInt(parts[2]);
            } catch (NumberFormatException ex) {
                return "-";
            }
            boolean nameField = parts[3].equalsIgnoreCase("name");
            boolean valueField = parts[3].equalsIgnoreCase("value");
            if (!nameField && !valueField) return "-";
            return plugin.leaderboards().getTopValue(type, place, nameField);
        }

        if (parts.length >= 2 && parts[0].equalsIgnoreCase("rank") && player != null) {
            String type = parts[1].toLowerCase();
            return plugin.leaderboards().getRank(type, player.getUniqueId());
        }

        return "";
    }
}
