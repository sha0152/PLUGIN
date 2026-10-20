package org.bukkit.plugin;

public class PluginMeta {
    private final String version;

    public PluginMeta(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
