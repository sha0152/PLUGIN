package com.velora.smp.economy;

import com.velora.smp.config.Settings;
import com.velora.smp.database.DatabaseManager;
import com.velora.smp.util.MoneyUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;

public final class EconomyService {
    private final JavaPlugin plugin;
    private Settings settings;
    private final DatabaseManager databaseManager;

    public EconomyService(JavaPlugin plugin, Settings settings, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.settings = settings;
        this.databaseManager = databaseManager;
    }

    public void reloadSettings(Settings settings) {
        this.settings = settings;
    }

    public CompletableFuture<BigDecimal> getBalance(UUID uuid, String name) {
        return databaseManager.supplyAsync(connection -> {
            ensureAccount(connection, uuid, name);
            try (PreparedStatement ps = connection.prepareStatement("SELECT balance FROM accounts WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return MoneyUtil.parseNonNegative(rs.getString(1), settings.moneyScale());
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return BigDecimal.ZERO.setScale(settings.moneyScale());
        });
    }

    public CompletableFuture<Boolean> transfer(UUID from, String fromName, UUID to, String toName, BigDecimal amount) {
        return databaseManager.supplyAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                ensureAccount(connection, from, fromName);
                ensureAccount(connection, to, toName);

                BigDecimal fromBalance = getBalanceInternal(connection, from);
                if (fromBalance.compareTo(amount) < 0) {
                    connection.rollback();
                    return false;
                }
                BigDecimal toBalance = getBalanceInternal(connection, to);
                BigDecimal newFrom = fromBalance.subtract(amount);
                BigDecimal newTo = cap(toBalance.add(amount));
                updateBalance(connection, from, fromName, newFrom);
                updateBalance(connection, to, toName, newTo);

                log(connection, "TRANSFER_OUT", from, to, amount, newFrom, "pay");
                log(connection, "TRANSFER_IN", from, to, amount, newTo, "pay");

                connection.commit();
                return true;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
                throw new RuntimeException(e);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        });
    }

    public CompletableFuture<BigDecimal> setBalance(UUID target, String name, BigDecimal amount, UUID actor, String reason) {
        return databaseManager.supplyAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                ensureAccount(connection, target, name);
                BigDecimal capped = cap(amount);
                updateBalance(connection, target, name, capped);
                log(connection, "ADMIN_SET", actor, target, capped, capped, reason);
                connection.commit();
                return capped;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<BigDecimal> addBalance(UUID target, String name, BigDecimal amount, UUID actor, String reason) {
        return databaseManager.supplyAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                ensureAccount(connection, target, name);
                BigDecimal existing = getBalanceInternal(connection, target);
                BigDecimal updated = cap(existing.add(amount));
                updateBalance(connection, target, name, updated);
                log(connection, "ADMIN_ADD", actor, target, amount, updated, reason);
                connection.commit();
                return updated;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<BigDecimal> takeBalance(UUID target, String name, BigDecimal amount, UUID actor, String reason) {
        return databaseManager.supplyAsync(connection -> {
            try {
                connection.setAutoCommit(false);
                ensureAccount(connection, target, name);
                BigDecimal existing = getBalanceInternal(connection, target);
                BigDecimal updated = existing.subtract(amount);
                if (updated.compareTo(BigDecimal.ZERO) < 0) {
                    updated = BigDecimal.ZERO.setScale(settings.moneyScale());
                }
                updateBalance(connection, target, name, updated);
                log(connection, "ADMIN_TAKE", actor, target, amount, updated, reason);
                connection.commit();
                return updated;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<BaltopEntry>> top(int page, int pageSize) {
        return databaseManager.supplyAsync(connection -> {
            int offset = Math.max(0, page - 1) * pageSize;
            List<BaltopEntry> entries = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement("SELECT name, balance FROM accounts ORDER BY CAST(balance AS REAL) DESC LIMIT ? OFFSET ?")) {
                ps.setInt(1, pageSize);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        entries.add(new BaltopEntry(rs.getString(1), MoneyUtil.parseNonNegative(rs.getString(2), settings.moneyScale())));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return entries;
        });
    }

    private void ensureAccount(Connection connection, UUID uuid, String name) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO accounts(uuid, name, balance, updated_at) VALUES(?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET name=excluded.name")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name == null ? uuid.toString() : name);
            ps.setString(3, BigDecimal.ZERO.setScale(settings.moneyScale()).toPlainString());
            ps.setLong(4, Instant.now().toEpochMilli());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private BigDecimal getBalanceInternal(Connection connection, UUID uuid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT balance FROM accounts WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return MoneyUtil.parseNonNegative(rs.getString(1), settings.moneyScale());
                }
            }
        }
        return BigDecimal.ZERO.setScale(settings.moneyScale());
    }

    private void updateBalance(Connection connection, UUID uuid, String name, BigDecimal balance) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE accounts SET balance=?, name=?, updated_at=? WHERE uuid=?")) {
            ps.setString(1, balance.setScale(settings.moneyScale()).toPlainString());
            ps.setString(2, name == null ? uuid.toString() : name);
            ps.setLong(3, Instant.now().toEpochMilli());
            ps.setString(4, uuid.toString());
            ps.executeUpdate();
        }
    }

    private void log(Connection connection, String type, UUID actor, UUID target, BigDecimal amount, BigDecimal balanceAfter, String reason) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO transactions(type, actor_uuid, target_uuid, amount, balance_after, reason, created_at) VALUES(?,?,?,?,?,?,?)")) {
            ps.setString(1, type);
            ps.setString(2, actor == null ? null : actor.toString());
            ps.setString(3, target == null ? null : target.toString());
            ps.setString(4, amount.toPlainString());
            ps.setString(5, balanceAfter == null ? null : balanceAfter.toPlainString());
            ps.setString(6, reason);
            ps.setLong(7, Instant.now().toEpochMilli());
            ps.executeUpdate();
        }
    }

    private BigDecimal cap(BigDecimal value) {
        BigDecimal scaled = value.setScale(settings.moneyScale());
        if (scaled.compareTo(settings.maxBalance()) > 0) {
            return settings.maxBalance().setScale(settings.moneyScale());
        }
        if (scaled.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(settings.moneyScale());
        }
        return scaled;
    }

    public int scale() {
        return settings.moneyScale();
    }

    public record BaltopEntry(String name, BigDecimal balance) {}
}
