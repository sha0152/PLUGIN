package org.bukkit.configuration.file;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class YamlConfiguration extends FileConfiguration {
    public YamlConfiguration(Map<String, Object> root) {
        super(root);
    }

    @SuppressWarnings("unchecked")
    public static YamlConfiguration loadConfiguration(File file) {
        if (!file.exists()) {
            return new YamlConfiguration(new LinkedHashMap<>());
        }
        try (FileInputStream in = new FileInputStream(file)) {
            Object data = new Yaml().load(in);
            if (data instanceof Map<?, ?> map) {
                return new YamlConfiguration((Map<String, Object>) map);
            }
        } catch (Exception ignored) {
        }
        return new YamlConfiguration(new LinkedHashMap<>());
    }
}
