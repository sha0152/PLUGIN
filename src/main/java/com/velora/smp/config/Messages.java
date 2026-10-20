package com.velora.smp.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
    private final JavaPlugin plugin;
    private final Map<String, String> values = new HashMap<>();

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        values.clear();
        File file = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        if (yml.isConfigurationSection("messages")) {
            for (String key : yml.getConfigurationSection("messages").getKeys(false)) {
                values.put(key, yml.getString("messages." + key, ""));
            }
        }
    }

    public String get(String key) {
        return values.getOrDefault(key, "Missing message: " + key);
    }
}
