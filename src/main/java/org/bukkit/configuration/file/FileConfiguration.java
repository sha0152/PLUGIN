package org.bukkit.configuration.file;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class FileConfiguration extends ConfigurationSection {
    public FileConfiguration(Map<String, Object> root) {
        super(root, "");
    }

    public static FileConfiguration empty() {
        return new FileConfiguration(new LinkedHashMap<>());
    }
}
