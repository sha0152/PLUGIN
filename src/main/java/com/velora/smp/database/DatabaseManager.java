package com.velora.smp.database;

import com.velora.smp.config.Settings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.bukkit.plugin.java.JavaPlugin;

public final class DatabaseManager {
    private final JavaPlugin plugin;
    private Settings settings;
    private final ExecutorService dbExecutor;
    private final String jdbcUrl;

    public DatabaseManager(JavaPlugin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "VeloraCore-DB");
            t.setDaemon(true);
            return t;
        });
        this.jdbcUrl = "jdbc:sqlite:" + new File(plugin.getDataFolder(), settings.databaseFile()).getAbsolutePath();
    }

    public void start() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL;");
            statement.execute("PRAGMA foreign_keys=ON;");
            statement.execute("CREATE TABLE IF NOT EXISTS schema_version(version INTEGER NOT NULL);");
            statement.execute("INSERT INTO schema_version(version) SELECT 1 WHERE NOT EXISTS(SELECT 1 FROM schema_version);");
            statement.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "uuid TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "balance TEXT NOT NULL," +
                    "updated_at INTEGER NOT NULL" +
                    ");");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_accounts_balance ON accounts(CAST(balance AS REAL) DESC);");
            statement.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "type TEXT NOT NULL," +
                    "actor_uuid TEXT," +
                    "target_uuid TEXT," +
                    "amount TEXT NOT NULL," +
                    "balance_after TEXT," +
                    "reason TEXT," +
                    "created_at INTEGER NOT NULL" +
                    ");");
            statement.execute("CREATE TABLE IF NOT EXISTS command_locks (" +
                    "lock_key TEXT PRIMARY KEY," +
                    "created_at INTEGER NOT NULL" +
                    ");");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize database", e);
        }
    }

    public void reloadSettings(Settings settings) {
        this.settings = settings;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    public <T> CompletableFuture<T> supplyAsync(Function<Connection, T> action) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                return action.apply(connection);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    public CompletableFuture<Path> backupNow() {
        return CompletableFuture.supplyAsync(() -> {
            File dataFolder = plugin.getDataFolder();
            Path dbPath = new File(dataFolder, settings.databaseFile()).toPath();
            File backupFolder = new File(dataFolder, settings.backupDirectory());
            if (!backupFolder.exists() && !backupFolder.mkdirs()) {
                throw new IllegalStateException("Cannot create backup directory");
            }
            Path backupPath = backupFolder.toPath().resolve("velora-" + Instant.now().toEpochMilli() + ".db");
            try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA wal_checkpoint(FULL);");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                Files.copy(dbPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                cleanupBackups(backupFolder.toPath());
                return backupPath;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    private void cleanupBackups(Path dir) throws IOException {
        List<Path> backups = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".db"))
                    .forEach(backups::add);
        }
        backups.sort(Comparator.comparing(Path::toString).reversed());
        for (int i = settings.backupRetention(); i < backups.size(); i++) {
            Files.deleteIfExists(backups.get(i));
        }
    }

    public void shutdown() {
        dbExecutor.shutdownNow();
    }
}
