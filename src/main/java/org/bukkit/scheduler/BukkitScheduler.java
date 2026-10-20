package org.bukkit.scheduler;

import org.bukkit.plugin.java.JavaPlugin;

public class BukkitScheduler {
    public void runTask(JavaPlugin plugin, Runnable runnable) {
        runnable.run();
    }
}
