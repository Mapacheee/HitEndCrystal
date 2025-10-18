package me.mapacheee.hitendcrystal.data;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.config.Config;
import me.mapacheee.hitendcrystal.config.ConfigService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ClickStorage {

    private final ConfigService configService;
    private HikariDataSource dataSource;

    private final Map<UUID, PlayerClickData> inMemoryData = new ConcurrentHashMap<>();
    private volatile List<PlayerClickData> topCache = new ArrayList<>();

    @Inject
    public ClickStorage(ConfigService configService) {
        this.configService = configService;
    }

    private synchronized void ensureInitialized() {
        if (dataSource == null) {
            initDatabase();
        }
    }

    private void initDatabase() {
        Config config = configService.getConfig();
        if (config == null) {
            throw new IllegalStateException("Config is not loaded yet!");
        }

        HikariConfig hikariConfig = new HikariConfig();

        if (config.database().type().equalsIgnoreCase("MYSQL")) {
            Config.Database.MysqlConfig mysql = config.database().mysql();
            hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true",
                mysql.host(), mysql.port(), mysql.database()));
            hikariConfig.setUsername(mysql.username());
            hikariConfig.setPassword(mysql.password());
            hikariConfig.setMaximumPoolSize(mysql.poolSize());
        } else {
            // SQLite
            String dbPath = HitEndCrystalPlugin.getInstance().getDataFolder().getAbsolutePath() + "/data.db";
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbPath);
            hikariConfig.setMaximumPoolSize(1);
        }

        hikariConfig.setPoolName("HitEndCrystal-Pool");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);
        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS player_clicks (
                uuid VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(16) NOT NULL,
                clicks INT NOT NULL DEFAULT 0
            )
            """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            HitEndCrystalPlugin.getInstance().getLogger().severe("Error creating table: " + e.getMessage());
        }
    }

    public CompletableFuture<PlayerClickData> loadPlayer(UUID uuid, String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            String sql = "SELECT * FROM player_clicks WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    PlayerClickData data = new PlayerClickData(
                        uuid,
                        rs.getString("player_name"),
                        rs.getInt("clicks")
                    );
                    updateCache(data);
                    return data;
                } else {
                    PlayerClickData newData = new PlayerClickData(uuid, playerName, 0);
                    savePlayer(newData);
                    updateCache(newData);
                    return newData;
                }
            } catch (SQLException e) {
                HitEndCrystalPlugin.getInstance().getLogger().severe("Error loading player: " + e.getMessage());
                PlayerClickData fallback = new PlayerClickData(uuid, playerName, 0);
                updateCache(fallback);
                return fallback;
            }
        });
    }

    public void savePlayer(PlayerClickData data) {
        updateCache(data);

        CompletableFuture.runAsync(() -> {
            ensureInitialized();
            String sql = """
                INSERT INTO player_clicks (uuid, player_name, clicks)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE player_name = ?, clicks = ?
                """;

            if (configService.getConfig().database().type().equalsIgnoreCase("SQLITE")) {
                sql = """
                    INSERT OR REPLACE INTO player_clicks (uuid, player_name, clicks)
                    VALUES (?, ?, ?)
                    """;
            }

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, data.uuid().toString());
                stmt.setString(2, data.playerName());
                stmt.setInt(3, data.clicks());

                if (configService.getConfig().database().type().equalsIgnoreCase("MYSQL")) {
                    stmt.setString(4, data.playerName());
                    stmt.setInt(5, data.clicks());
                }

                stmt.executeUpdate();
            } catch (SQLException e) {
                HitEndCrystalPlugin.getInstance().getLogger().severe("Error saving player: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<List<PlayerClickData>> getTopPlayers(int limit) {
        if (!topCache.isEmpty()) {
            List<PlayerClickData> snapshot = topCache.stream().limit(limit).collect(Collectors.toList());
            return CompletableFuture.completedFuture(snapshot);
        }

        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            String sql = "SELECT * FROM player_clicks ORDER BY clicks DESC LIMIT ?";
            List<PlayerClickData> topPlayers = new ArrayList<>();

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    topPlayers.add(new PlayerClickData(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getInt("clicks")
                    ));
                }
            } catch (SQLException e) {
                HitEndCrystalPlugin.getInstance().getLogger().severe("Error getting top players: " + e.getMessage());
            }

            updateTopCache(topPlayers);

            return topPlayers;
        });
    }

    public CompletableFuture<Integer> getPlayerPosition(UUID uuid) {
        if (inMemoryData.containsKey(uuid) && !topCache.isEmpty()) {
            PlayerClickData target = inMemoryData.get(uuid);
            int pos = 1;
            for (PlayerClickData p : topCache) {
                if (p.uuid().equals(uuid)) return CompletableFuture.completedFuture(pos);
                pos++;
            }
            int higher = 0;
            for (PlayerClickData p : inMemoryData.values()) {
                if (p.clicks() > target.clicks()) higher++;
            }
            return CompletableFuture.completedFuture(higher + 1);
        }

        return CompletableFuture.supplyAsync(() -> {
            ensureInitialized();
            String sql = """
                SELECT COUNT(*) + 1 as position
                FROM player_clicks
                WHERE clicks > (SELECT clicks FROM player_clicks WHERE uuid = ?)
                """;

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("position");
                }
            } catch (SQLException e) {
                HitEndCrystalPlugin.getInstance().getLogger().severe("Error getting player position: " + e.getMessage());
            }

            return -1;
        });
    }

    public void resetPlayer(UUID uuid) {
        inMemoryData.remove(uuid);
        rebuildTopCacheFromInMemory();

        CompletableFuture.runAsync(() -> {
            ensureInitialized();
            String sql = "UPDATE player_clicks SET clicks = 0 WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                HitEndCrystalPlugin.getInstance().getLogger().severe("Error resetting player: " + e.getMessage());
            }
        });
    }

    private synchronized void updateCache(PlayerClickData data) {
        inMemoryData.put(data.uuid(), data);
        rebuildTopCacheFromInMemory();
    }

    private synchronized void updateTopCache(List<PlayerClickData> list) {
        this.topCache = new ArrayList<>(list);
    }

    private synchronized void rebuildTopCacheFromInMemory() {
        List<PlayerClickData> sorted = inMemoryData.values().stream()
            .sorted(Comparator.comparingInt(PlayerClickData::clicks).reversed())
            .collect(Collectors.toList());
        this.topCache = sorted;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
