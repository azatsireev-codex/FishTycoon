package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.LeaderboardEntry;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardService {
    private final FishTycoonPlugin plugin;
    private final Map<String, List<LeaderboardEntry>> cached = new ConcurrentHashMap<>();
    private int taskId = -1;

    public LeaderboardService(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        refreshAll();
        long periodTicks = 20L * 60L;
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::refreshAll, periodTicks, periodTicks).getTaskId();
    }

    public void stop() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
    }

    public void refreshAll() {
        cached.put("rod", plugin.repo().topByRodLevel(10));
        cached.put("rebirth", plugin.repo().topByRebirth(10));
        cached.put("caught", plugin.repo().topByCaught(10));
    }

    public String getTopValue(String type, int place, boolean nameField) {
        List<LeaderboardEntry> list = cached.getOrDefault(type, Collections.emptyList());
        if (place < 1 || place > list.size()) return "-";
        LeaderboardEntry entry = list.get(place - 1);
        return nameField ? entry.name() : String.valueOf(entry.value());
    }

    public String getRank(String type, UUID uuid) {
        List<LeaderboardEntry> list = cached.getOrDefault(type, Collections.emptyList());
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).uuid().equals(uuid)) return String.valueOf(i + 1);
        }
        return "-";
    }
}
