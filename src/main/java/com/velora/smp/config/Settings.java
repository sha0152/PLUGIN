package com.velora.smp.config;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public record Settings(
        String displayName,
        String prefix,
        Set<String> enabledModules,
        int moneyScale,
        BigDecimal maxBalance,
        boolean allowSelfPay,
        BigDecimal defaultPrice,
        BigDecimal safeFallbackPrice,
        Set<String> sellExclusions,
        boolean includeOffhand,
        boolean includeArmor,
        BigDecimal minSale,
        BigDecimal maxSale,
        String databaseFile,
        String backupDirectory,
        int backupRetention,
        long backupIntervalMinutes
) {

    public static Settings load(JavaPlugin plugin) {
        FileConfiguration c = plugin.getConfig();
        Set<String> enabled = new HashSet<>();
        if (c.isConfigurationSection("modules")) {
            for (String k : c.getConfigurationSection("modules").getKeys(false)) {
                if (c.getBoolean("modules." + k, false)) {
                    enabled.add(k.toLowerCase(Locale.ROOT));
                }
            }
        }
        return new Settings(
                c.getString("branding.display-name", "Velora SMP"),
                c.getString("branding.prefix", "<gold>Velora</gold> "),
                enabled,
                Math.max(0, c.getInt("economy.scale", 2)),
                bd(c.getString("economy.max-balance", "1000000000.00"), "1000000000.00"),
                c.getBoolean("economy.allow-self-pay", false),
                bd(c.getString("selling.default-price", "8.00"), "8.00"),
                bd(c.getString("selling.safe-fallback-price", "1.00"), "1.00"),
                normalize(c.getStringList("selling.exclusions")),
                c.getBoolean("selling.include-offhand", false),
                c.getBoolean("selling.include-armor", false),
                bd(c.getString("selling.min-sale", "0.01"), "0.01"),
                bd(c.getString("selling.max-sale", "1000000.00"), "1000000.00"),
                c.getString("database.file", "velora.db"),
                c.getString("database.backup-directory", "backups"),
                Math.max(1, c.getInt("database.backup-retention", 24)),
                Math.max(1, c.getLong("database.backup-interval-minutes", 60))
        );
    }

    private static BigDecimal bd(String v, String fallback) {
        try {
            return new BigDecimal(v);
        } catch (Exception ignored) {
            return new BigDecimal(fallback);
        }
    }

    private static Set<String> normalize(List<String> values) {
        Set<String> out = new HashSet<>();
        for (String value : values) {
            out.add(value.toUpperCase(Locale.ROOT));
        }
        return out;
    }

    public boolean moduleEnabled(String module) {
        return enabledModules.contains(module.toLowerCase(Locale.ROOT));
    }
}
