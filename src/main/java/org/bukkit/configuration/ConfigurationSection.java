package org.bukkit.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurationSection {
    protected final Map<String, Object> root;
    protected final String path;

    public ConfigurationSection(Map<String, Object> root, String path) {
        this.root = root;
        this.path = path;
    }

    public Set<String> getKeys(boolean deep) {
        Object section = getObject(path);
        if (section instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).keySet();
        }
        return Collections.emptySet();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String def) {
        Object o = getObject(absolute(key));
        return o == null ? def : String.valueOf(o);
    }

    public int getInt(String key, int def) {
        Object o = getObject(absolute(key));
        if (o instanceof Number n) return n.intValue();
        try { return o == null ? def : Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return def; }
    }

    public long getLong(String key, long def) {
        Object o = getObject(absolute(key));
        if (o instanceof Number n) return n.longValue();
        try { return o == null ? def : Long.parseLong(String.valueOf(o)); } catch (Exception e) { return def; }
    }

    public boolean getBoolean(String key, boolean def) {
        Object o = getObject(absolute(key));
        if (o instanceof Boolean b) return b;
        if (o == null) return def;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    public List<String> getStringList(String key) {
        Object o = getObject(absolute(key));
        if (o instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public boolean isConfigurationSection(String key) {
        return getObject(absolute(key)) instanceof Map;
    }

    public ConfigurationSection getConfigurationSection(String key) {
        if (!isConfigurationSection(key)) return null;
        return new ConfigurationSection(root, absolute(key));
    }

    protected String absolute(String key) {
        return path == null || path.isEmpty() ? key : path + "." + key;
    }

    protected Object getObject(String key) {
        if (key == null || key.isEmpty()) return root;
        String[] parts = key.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = ((Map<?, ?>) map).get(part);
            if (current == null) return null;
        }
        return current;
    }
}
