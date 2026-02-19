package com.fishtycoon.service;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.data.PlayerDataRepository;
import com.fishtycoon.model.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionService {
    private final FishTycoonPlugin plugin;
    private final PlayerDataRepository repository;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public SessionService(FishTycoonPlugin plugin, PlayerDataRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void load(UUID uuid, Runnable onDone) {
        repository.loadPlayer(uuid).whenComplete((data, throwable) -> {
            if (throwable != null) {
                plugin.getLogger().warning("Could not load player data for " + uuid + ": " + throwable.getMessage());
                data = new PlayerData(uuid);
            }
            cache.put(uuid, data);
            if (onDone != null) {
                PlayerData finalData = data;
                plugin.getServer().getScheduler().runTask(plugin, onDone);
            }
        });
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, PlayerData::new);
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) repository.savePlayer(data);
    }

    public void save(PlayerData data) {
        repository.savePlayer(data);
    }

    public void flushAll() {
        cache.values().forEach(repository::savePlayer);
    }
}
