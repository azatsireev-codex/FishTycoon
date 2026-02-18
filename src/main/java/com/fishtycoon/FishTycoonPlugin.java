package com.fishtycoon;

import com.fishtycoon.command.FishCommand;
import com.fishtycoon.config.ConfigManager;
import com.fishtycoon.data.PlayerDataRepository;
import com.fishtycoon.listener.BackpackItemListener;
import com.fishtycoon.listener.FishingListener;
import com.fishtycoon.listener.MenuListener;
import com.fishtycoon.listener.PlayerSessionListener;
import com.fishtycoon.placeholder.FishTycoonPlaceholderExpansion;
import com.fishtycoon.service.*;
import com.fishtycoon.util.KeyRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class FishTycoonPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private KeyRegistry keyRegistry;
    private PlayerDataRepository repository;
    private SessionService sessionService;
    private Economy economy;

    private RodService rodService;
    private BackpackService backpackService;
    private LocationService locationService;
    private FishService fishService;
    private EconomyService economyService;
    private RebirthService rebirthService;
    private MenuService menuService;
    private LeaderboardService leaderboardService;
    private FishTycoonPlaceholderExpansion placeholderExpansion;

    @Override
    public void onEnable() {
        if (!setupVault()) {
            getLogger().severe("Vault economy not found. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configManager = new ConfigManager(this);
        configManager.loadAll();
        keyRegistry = new KeyRegistry(this);

        repository = new PlayerDataRepository(this);
        try {
            repository.init();
        } catch (SQLException e) {
            getLogger().severe("Could not initialize database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        sessionService = new SessionService(this, repository);
        rodService = new RodService(this);
        backpackService = new BackpackService(this);
        locationService = new LocationService(this);
        fishService = new FishService(this);
        economyService = new EconomyService(this);
        rebirthService = new RebirthService(this);
        menuService = new MenuService(this);
        leaderboardService = new LeaderboardService(this);
        leaderboardService.start();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderExpansion = new FishTycoonPlaceholderExpansion(this);
            placeholderExpansion.register();
        }

        FishCommand command = new FishCommand(this);
        getCommand("fish").setExecutor(command);
        getCommand("fish").setTabCompleter(command);

        Bukkit.getPluginManager().registerEvents(new PlayerSessionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FishingListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BackpackItemListener(this), this);
    }

    @Override
    public void onDisable() {
        if (sessionService != null) sessionService.flushAll();
        if (leaderboardService != null) leaderboardService.stop();
        if (placeholderExpansion != null) placeholderExpansion.unregister();
        if (repository != null) repository.close();
    }

    private boolean setupVault() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public ConfigManager cfg() { return configManager; }
    public KeyRegistry keys() { return keyRegistry; }
    public SessionService sessions() { return sessionService; }
    public Economy economy() { return economy; }
    public RodService rods() { return rodService; }
    public BackpackService backpacks() { return backpackService; }
    public LocationService locations() { return locationService; }
    public FishService fish() { return fishService; }
    public EconomyService economyService() { return economyService; }
    public RebirthService rebirth() { return rebirthService; }
    public MenuService menus() { return menuService; }
    public LeaderboardService leaderboards() { return leaderboardService; }
    public PlayerDataRepository repo() { return repository; }
}
