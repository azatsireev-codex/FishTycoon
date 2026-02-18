package com.fishtycoon.command;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.FishDefinition;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FishCommand implements CommandExecutor, TabCompleter {
    private final FishTycoonPlugin plugin;

    public FishCommand(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Msg.c("<aqua>/fish rod|backpack|upgrades|bestiary|sell|locations|rebirth"));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "rod" -> handleRod(player);
            case "backpack" -> handleBackpack(player, args);
            case "upgrades" -> plugin.menus().openUpgrades(player);
            case "bestiary" -> plugin.menus().openBestiary(player);
            case "sell" -> plugin.economyService().sellAllFish(player);
            case "locations" -> handleLocations(player);
            case "rebirth" -> handleRebirth(player, args);
            case "admin" -> handleAdmin(player, args);
            default -> player.sendMessage(Msg.c("<red>Unknown subcommand."));
        }
        return true;
    }

    private void handleRod(Player player) {
        PlayerData data = plugin.sessions().get(player.getUniqueId());
        long now = System.currentTimeMillis();
        long next = data.getLastRodClaimAt() + plugin.cfg().getRodClaimCooldownMs();
        if (now < next) {
            long remain = (next - now) / 1000;
            player.sendMessage(Msg.c("<red>Rod restore cooldown: " + remain + "s"));
            return;
        }
        player.getInventory().addItem(plugin.rods().createStarterRod(player, data));
        data.setLastRodClaimAt(now);
        player.sendMessage(Msg.c("<green>Starter rod restored."));
    }

    private void handleBackpack(Player player, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("upgrade")) {
            if (plugin.backpacks().upgradeBackpack(player)) {
                plugin.backpacks().refreshBackpackItem(player);
            }
            return;
        }
        plugin.backpacks().openBackpackSummary(player);
        player.sendMessage(Msg.c("<yellow>Use <white>/fish backpack upgrade <yellow>to increase capacity."));
    }

    private void handleLocations(Player player) {
        player.sendMessage(Msg.c("<gold>Locations:"));
        plugin.cfg().getLocationDefinitions().values().forEach(l -> player.sendMessage(Msg.c("<gray>- <white>" + l.displayName() + " <dark_gray>(Lvl " + l.requiredLevel() + ")")));
    }

    private void handleRebirth(Player player, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            plugin.rebirth().rebirth(player);
            return;
        }
        player.sendMessage(Msg.c("<yellow>Type <white>/fish rebirth confirm <yellow>to prestige."));
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("fishtycoon.admin.*")) {
            player.sendMessage(Msg.c("<red>No permission."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Msg.c("<yellow>/fish admin reload|givefish|setlevel|addxp|debug"));
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.cfg().loadAll();
                player.sendMessage(Msg.c("<green>Config reloaded."));
            }
            case "givefish" -> {
                if (args.length < 4) {
                    player.sendMessage("/fish admin givefish <player> <fishId> [size]");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) return;
                FishDefinition fish = plugin.cfg().getFishDefinitions().get(args[3]);
                if (fish == null) {
                    player.sendMessage(Msg.c("<red>Unknown fish id."));
                    return;
                }
                double size = args.length >= 5 ? Double.parseDouble(args[4]) : plugin.fish().rollSize(fish, plugin.sessions().get(target.getUniqueId()));
                boolean added = plugin.backpacks().addFish(plugin.sessions().get(target.getUniqueId()), fish.id(), size, 1);
                if (!added) {
                    player.sendMessage(Msg.c("<red>Target backpack is full."));
                    return;
                }
                plugin.backpacks().refreshBackpackItem(target);
                player.sendMessage(Msg.c("<green>Fish added to target backpack."));
            }
            case "setlevel" -> {
                if (args.length < 4) return;
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) return;
                plugin.sessions().get(target.getUniqueId()).setRodLevel(Integer.parseInt(args[3]));
                player.sendMessage(Msg.c("<green>Level set."));
            }
            case "addxp" -> {
                if (args.length < 4) return;
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) return;
                PlayerData data = plugin.sessions().get(target.getUniqueId());
                data.setRodXp(data.getRodXp() + Double.parseDouble(args[3]));
                player.sendMessage(Msg.c("<green>XP added."));
            }
            case "debug" -> {
                var loc = plugin.locations().currentLocation(player);
                var hs = plugin.locations().currentHotspot(loc, player);
                player.sendMessage(Msg.c("<gray>Location: <white>" + (loc == null ? "none" : loc.id())));
                player.sendMessage(Msg.c("<gray>Hotspot: <white>" + (hs == null ? "none" : hs.id())));
                PlayerData data = plugin.sessions().get(player.getUniqueId());
                player.sendMessage(Msg.c("<gray>Backpack: <white>Lvl " + data.getBackpackLevel() + " " + data.backpackUsed() + "/" + plugin.backpacks().capacity(data)));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("rod", "backpack", "upgrades", "bestiary", "sell", "locations", "rebirth", "admin");
        if (args.length == 2 && args[0].equalsIgnoreCase("backpack")) return List.of("upgrade");
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) return List.of("reload", "givefish", "setlevel", "addxp", "debug");
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("givefish")) return new ArrayList<>(plugin.cfg().getFishDefinitions().keySet());
        return List.of();
    }
}
