package org.bukkit.plugin.java;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginMeta;

public class JavaPlugin implements Plugin {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final File dataFolder = new File(".", "build-plugin-data");
    private final Map<String, PluginCommand> commands = new HashMap<>();
    private FileConfiguration config;

    public void onEnable() {}

    public void onDisable() {}

    public File getDataFolder() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    public void saveDefaultConfig() {
        saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        }
        return config;
    }

    public void saveResource(String resourcePath, boolean replace) {
        try {
            File target = new File(getDataFolder(), resourcePath);
            if (target.exists() && !replace) {
                return;
            }
            File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (in != null) {
                    Files.copy(in, target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public PluginCommand getCommand(String name) {
        return commands.computeIfAbsent(name, k -> new PluginCommand());
    }

    public Logger getLogger() {
        return logger;
    }

    public PluginMeta getPluginMeta() {
        return new PluginMeta("1.0.0");
    }

    public Server getServer() {
        return new Server();
    }
}
