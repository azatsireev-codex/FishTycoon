package com.fishtycoon.data;

import com.fishtycoon.FishTycoonPlugin;
import com.fishtycoon.model.BackpackFishEntry;
import com.fishtycoon.model.PlayerData;
import com.fishtycoon.model.PlayerFishStat;
import com.fishtycoon.model.UpgradeType;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDataRepository {
    private final FishTycoonPlugin plugin;
    private Connection connection;

    public PlayerDataRepository(FishTycoonPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "players.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, rod_level INTEGER, rod_xp REAL, rebirth_count INTEGER, last_rod_claim INTEGER, upgrades TEXT, backpack_level INTEGER DEFAULT 1, backpack_data TEXT DEFAULT '')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bestiary (uuid TEXT, fish_id TEXT, times_caught INTEGER, largest_size REAL, first_caught_at INTEGER, PRIMARY KEY(uuid, fish_id))");
        }
        ensurePlayersColumns();
    }

    private void ensurePlayersColumns() throws SQLException {
        Set<String> columns = new java.util.HashSet<>();
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(players)")) {
            while (rs.next()) columns.add(rs.getString("name"));
        }
        if (!columns.contains("backpack_level")) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("ALTER TABLE players ADD COLUMN backpack_level INTEGER DEFAULT 1");
            }
        }
        if (!columns.contains("backpack_data")) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("ALTER TABLE players ADD COLUMN backpack_data TEXT DEFAULT ''");
            }
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException ignored) {}
    }

    public CompletableFuture<PlayerData> loadPlayer(UUID uuid) {
        CompletableFuture<PlayerData> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData data = new PlayerData(uuid);
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM players WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.setRodLevel(rs.getInt("rod_level"));
                        data.setRodXp(rs.getDouble("rod_xp"));
                        data.setRebirthCount(rs.getInt("rebirth_count"));
                        data.setLastRodClaimAt(rs.getLong("last_rod_claim"));
                        data.setBackpackLevel(Math.max(1, rs.getInt("backpack_level")));
                        String upgrades = rs.getString("upgrades");
                        if (upgrades != null && !upgrades.isBlank()) {
                            for (String entry : upgrades.split(";")) {
                                String[] kv = entry.split(":");
                                if (kv.length == 2) {
                                    data.getUpgrades().put(UpgradeType.valueOf(kv[0]), Integer.parseInt(kv[1]));
                                }
                            }
                        }
                        String backpackData = rs.getString("backpack_data");
                        if (backpackData != null && !backpackData.isBlank()) {
                            for (String entry : backpackData.split(";")) {
                                String[] parts = entry.split("@");
                                if (parts.length != 3) continue;
                                data.getBackpackEntries().add(new BackpackFishEntry(parts[0], Double.parseDouble(parts[1]), Integer.parseInt(parts[2])));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
                return;
            }

            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM bestiary WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        PlayerFishStat stat = new PlayerFishStat();
                        stat.setTimesCaught(rs.getInt("times_caught"));
                        stat.setLargestSize(rs.getDouble("largest_size"));
                        stat.setFirstCaughtAt(rs.getLong("first_caught_at"));
                        data.getBestiary().put(rs.getString("fish_id"), stat);
                    }
                }
            } catch (SQLException e) {
                future.completeExceptionally(e);
                return;
            }
            future.complete(data);
        });
        return future;
    }

    public void savePlayer(PlayerData data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String upgradeString = String.join(";", data.getUpgrades().entrySet().stream().map(e -> e.getKey().name() + ":" + e.getValue()).toList());
            String backpackString = String.join(";", data.getBackpackEntries().stream().map(e -> e.getFishId() + "@" + e.getSize() + "@" + e.getAmount()).toList());
            try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO players(uuid, rod_level, rod_xp, rebirth_count, last_rod_claim, upgrades, backpack_level, backpack_data) VALUES(?,?,?,?,?,?,?,?)")) {
                ps.setString(1, data.getUuid().toString());
                ps.setInt(2, data.getRodLevel());
                ps.setDouble(3, data.getRodXp());
                ps.setInt(4, data.getRebirthCount());
                ps.setLong(5, data.getLastRodClaimAt());
                ps.setString(6, upgradeString);
                ps.setInt(7, data.getBackpackLevel());
                ps.setString(8, backpackString);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to save player data: " + e.getMessage());
            }

            for (Map.Entry<String, PlayerFishStat> entry : data.getBestiary().entrySet()) {
                PlayerFishStat stat = entry.getValue();
                try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO bestiary(uuid, fish_id, times_caught, largest_size, first_caught_at) VALUES(?,?,?,?,?)")) {
                    ps.setString(1, data.getUuid().toString());
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, stat.getTimesCaught());
                    ps.setDouble(4, stat.getLargestSize());
                    ps.setLong(5, stat.getFirstCaughtAt());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to save bestiary stat: " + e.getMessage());
                }
            }
        });
    }
}
